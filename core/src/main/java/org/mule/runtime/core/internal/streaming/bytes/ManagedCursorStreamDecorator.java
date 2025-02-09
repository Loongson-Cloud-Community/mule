/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.streaming.bytes;

import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.internal.streaming.CursorProviderJanitor;
import org.mule.runtime.core.internal.streaming.StreamingGhostBuster;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A decorator which allows performing management tasks over a {@link CursorStream}
 *
 * @since 4.1.6
 */
class ManagedCursorStreamDecorator extends CursorStream {

  private ManagedCursorStreamProvider managedCursorProvider;
  private CursorStreamProvider exposedProvider;
  private final CursorStream delegate;
  private final CursorProviderJanitor janitor;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * Creates a new instance. Notice that it receives a {@code managedCursorProvider} so that a hard reference is kept during the
   * lifespan of this cursor. This prevents the {@link StreamingGhostBuster} from closing the provider in corner cases in which
   * this cursor is still referenced but the provider is not.
   *
   * @param managedCursorProvider the managed provider that opened this cursor
   * @param delegate              the delegate cursor
   * @param janitor               the cursor's janitor object
   */
  ManagedCursorStreamDecorator(ManagedCursorStreamProvider managedCursorProvider,
                               CursorStream delegate,
                               CursorProviderJanitor janitor) {
    this.managedCursorProvider = managedCursorProvider;
    exposedProvider = managedCursorProvider;
    this.delegate = delegate;
    this.janitor = janitor;
  }

  @Override
  public void close() throws IOException {
    if (closed.compareAndSet(false, true)) {
      try {
        delegate.close();
      } finally {
        if (managedCursorProvider != null) {
          exposedProvider = (CursorStreamProvider) managedCursorProvider.getDelegate();
          managedCursorProvider = null;
        }
        janitor.releaseCursor(delegate);
      }
    }
  }

  @Override
  public long getPosition() {
    return delegate.getPosition();
  }

  @Override
  public void seek(long position) throws IOException {
    delegate.seek(position);
  }

  @Override
  public boolean isReleased() {
    return delegate.isReleased();
  }

  @Override
  public void release() {
    delegate.release();
  }

  @Override
  public CursorProvider getProvider() {
    return exposedProvider;
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return delegate.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return delegate.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return delegate.skip(n);
  }

  @Override
  public int available() throws IOException {
    return delegate.available();
  }

  @Override
  public void mark(int readlimit) {
    delegate.mark(readlimit);
  }

  @Override
  public void reset() throws IOException {
    delegate.reset();
  }

  @Override
  public boolean markSupported() {
    return delegate.markSupported();
  }
}
