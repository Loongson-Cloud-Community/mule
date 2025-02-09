/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.streaming.bytes;

import static java.lang.Math.min;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.internal.streaming.bytes.ByteStreamingConstants.DEFAULT_BUFFER_BUCKET_SIZE;
import static org.mule.runtime.core.internal.streaming.bytes.ByteStreamingConstants.DEFAULT_BUFFER_POOL_SIZE;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.core.api.streaming.bytes.ManagedByteBufferWrapper;
import org.mule.runtime.core.internal.streaming.DefaultMemoryManager;
import org.mule.runtime.core.internal.streaming.MemoryManager;

import java.nio.ByteBuffer;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.apache.commons.pool2.KeyedObjectPool;
import org.slf4j.Logger;
import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.PoolObjectFactory;
import org.vibur.objectpool.PoolService;
import org.vibur.objectpool.util.MultithreadConcurrentQueueCollection;

/**
 * {@link MemoryBoundByteBufferManager} implementation which pools instances for better performance.
 * <p>
 * Buffers are kept in separate pools depending on their capacity.
 * <p>
 * Idle capacity pools are automatically expired, but items in each pool are never reclaimed.
 * <p>
 * Unlike traditional pools, if a pool is exhausted then an ephemeral {@link ByteBuffer} will be produced. That instance must
 * still be returned through the {@link #deallocate(ByteBuffer)} method.
 *
 * @since 4.0
 */
public class PoolingByteBufferManager extends MemoryBoundByteBufferManager implements Disposable {

  private static final Logger LOGGER = getLogger(PoolingByteBufferManager.class);

  private final int size;

  private BufferPool defaultSizePool;

  /**
   * Using a cache of pools instead of a {@link KeyedObjectPool} because performance tests indicates that this option is slightly
   * faster, plus it gives us the ability to expire unfrequent capacity buffers without the use of a reaper thread (those
   * performance test did not include such a reaper, so it's very possible that this is more than just slightly faster)
   */
  private final LoadingCache<Integer, BufferPool> customSizePools = Caffeine.newBuilder()
      .expireAfterAccess(5, MINUTES)
      .removalListener((RemovalListener<Integer, BufferPool>) (key, value, cause) -> {
        try {
          value.close();
        } catch (Exception e) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found exception trying to dispose buffer pool for capacity " + key, e);
          }
        }
      }).build(this::newBufferPool);

  /**
   * Creates a new instance which allows the pool to grow up to 70% of the runtime's max memory and has a wait timeout of 10
   * seconds. The definition of max memory is that of {@link MemoryManager#getMaxMemory()}
   */
  public PoolingByteBufferManager() {
    this(new DefaultMemoryManager(), DEFAULT_BUFFER_POOL_SIZE, DEFAULT_BUFFER_BUCKET_SIZE);
  }

  /**
   * Creates a new instance which allows the pool to grow up to 50% of calling {@link MemoryManager#getMaxMemory()} on the given
   * {@code memoryManager}, and has {@code waitTimeoutMillis} as wait timeout.
   *
   * @param memoryManager a {@link MemoryManager} used to determine the runtime's max memory
   */
  public PoolingByteBufferManager(MemoryManager memoryManager, int size, int bufferSize) {
    super(memoryManager);
    this.size = size;
    defaultSizePool = newBufferPool(bufferSize);
  }

  private BufferPool newBufferPool(Integer capacity) {
    return new BufferPool(size, capacity);
  }

  private BufferPool getBufferPool(int capacity) {
    return capacity == DEFAULT_BUFFER_BUCKET_SIZE ? defaultSizePool : customSizePools.get(capacity);
  }

  @Override
  public ManagedByteBufferWrapper allocateManaged(int capacity) {
    try {
      return getBufferPool(capacity).take();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new MuleRuntimeException(createStaticMessage("Could not allocate byte buffer. " + e.getMessage()), e);
    }
  }

  @Override
  public void dispose() {
    try {
      defaultSizePool.close();
    } catch (Exception e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Error disposing default capacity byte buffers pool", e);
      }
    }
    try {
      customSizePools.invalidateAll();
    } catch (Exception e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Error disposing mixed capacity byte buffers pool", e);
      }
    }
  }

  private class BufferPool {

    private final PoolService<ManagedByteBufferWrapper> pool;
    private final PoolObjectFactory<ManagedByteBufferWrapper> factory;
    private final int bufferCapacity;

    private BufferPool(int size, int bufferCapacity) {
      this.bufferCapacity = bufferCapacity;
      factory = new PoolObjectFactory<ManagedByteBufferWrapper>() {

        @Override
        public ManagedByteBufferWrapper create() {
          return new ManagedByteBufferWrapper(allocateIfFits(bufferCapacity), buffer -> returnBuffer(buffer));
        }

        @Override
        public boolean readyToTake(ManagedByteBufferWrapper buffer) {
          return true;
        }

        @Override
        public boolean readyToRestore(ManagedByteBufferWrapper buffer) {
          buffer.getDelegate().clear();
          return true;
        }

        @Override
        public void destroy(ManagedByteBufferWrapper buffer) {
          doDeallocate(buffer.getDelegate());
        }
      };

      pool = new ConcurrentPool<>(new MultithreadConcurrentQueueCollection<>(size),
                                  factory, min(getRuntime().availableProcessors(), size), size, false);
    }

    private ManagedByteBufferWrapper take() {
      ManagedByteBufferWrapper buffer = pool.tryTake();
      if (buffer == null) {
        buffer = new ManagedByteBufferWrapper(allocateIfFits(bufferCapacity), b -> doDeallocate(b.getDelegate()));
      }

      return buffer;
    }

    private void returnBuffer(ManagedByteBufferWrapper buffer) {
      pool.restore(buffer);
    }

    private void close() {
      pool.close();
    }
  }
}
