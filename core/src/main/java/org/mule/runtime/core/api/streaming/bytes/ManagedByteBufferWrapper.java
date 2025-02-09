/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming.bytes;

import org.mule.runtime.core.api.util.func.CheckedConsumer;

import java.nio.ByteBuffer;

/**
 * Wraps a {@link ByteBuffer} that is being managed the runtime.
 * <p>
 * Once all consumers are done with it and the buffer is no longer needed, the {@link #release()} method <b>MUST</b> be invoked.
 *
 * @since 4.3.0
 */
public class ManagedByteBufferWrapper {

  private final ByteBuffer delegate;
  private final CheckedConsumer<ManagedByteBufferWrapper> deallocator;

  /**
   * Creates a new instance
   *
   * @param delegate      the managed {@link ByteBuffer}
   * @param releaseAction action to be executed when the {@link #release()} method is invoked
   */
  public ManagedByteBufferWrapper(ByteBuffer delegate, CheckedConsumer<ManagedByteBufferWrapper> releaseAction) {
    this.delegate = delegate;
    this.deallocator = releaseAction;
  }

  /**
   * @return the managed {@link ByteBuffer}
   */
  public ByteBuffer getDelegate() {
    return delegate;
  }

  /**
   * Releases the managed buffer
   */
  public void release() {
    deallocator.accept(this);
  }
}
