/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming.bytes.factory;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.functional.Either;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.streaming.StreamingManager;
import org.mule.runtime.core.api.streaming.bytes.ByteBufferManager;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;

import java.io.InputStream;

/**
 * An implementation of {@link AbstractCursorStreamProviderFactory} which always generates instances of
 * {@link InMemoryCursorStreamProvider}
 *
 * @see InMemoryCursorStreamProvider
 * @since 4.0
 */
@NoExtend
public class InMemoryCursorStreamProviderFactory extends AbstractCursorStreamProviderFactory {

  private final InMemoryCursorStreamConfig config;

  /**
   * Creates a new instance
   *
   * @param config        the config for the generated providers
   * @param bufferManager the {@link ByteBufferManager} that will be used to allocate all buffers
   */
  public InMemoryCursorStreamProviderFactory(ByteBufferManager bufferManager,
                                             InMemoryCursorStreamConfig config,
                                             StreamingManager streamingManager) {
    super(bufferManager, streamingManager);
    this.config = config;
  }

  @Override
  protected Object resolve(InputStream inputStream, EventContext eventContext, ComponentLocation originatingLocation) {
    return doResolve(inputStream, originatingLocation);
  }

  /**
   * {@inheritDoc}
   *
   * @return a new {@link InMemoryCursorStreamProvider} wrapped in an {@link Either}
   */
  @Override
  protected Object resolve(InputStream inputStream, CoreEvent event, ComponentLocation originatingLocation) {
    return doResolve(inputStream, originatingLocation);
  }

  private Object doResolve(InputStream inputStream, ComponentLocation originatingLocation) {
    return new InMemoryCursorStreamProvider(inputStream, config, getBufferManager(), originatingLocation,
                                            trackCursorProviderClose);
  }
}
