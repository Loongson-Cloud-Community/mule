/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming.bytes;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

/**
 * Manages components in charge of streaming bytes so that the runtime can keep track of them, enforce policies and make sure that
 * all resources are reclaimed once no longer needed.
 *
 * @since 4.0
 */
@NoImplement
public interface ByteStreamingManager {

  /**
   * Creates a {@link CursorStreamProviderFactory} which buffers in memory
   *
   * @param config the configuration for the produced {@link CursorStreamProvider} instances
   * @return a new {@link CursorStreamProviderFactory}
   */
  CursorStreamProviderFactory getInMemoryCursorProviderFactory(InMemoryCursorStreamConfig config);

  /**
   * Creates a null object implementation of {@link CursorStreamProviderFactory}
   *
   * @return a new {@link CursorStreamProviderFactory}
   */
  CursorStreamProviderFactory getNullCursorProviderFactory();

  /**
   * @return The default implementation of {@link CursorStreamProviderFactory}
   */
  CursorStreamProviderFactory getDefaultCursorProviderFactory();

  /**
   * Creates a {@link CursorStreamProviderFactory} which buffers in disk.
   * <p>
   * Functionality has been available since 4.0, but was made available through this interface in 4.5.0.
   *
   * @param config the configuration for the produced {@link CursorStreamProvider} instances
   * @return a new {@link CursorStreamProviderFactory}
   * @since 4.5.0
   */
  default CursorStreamProviderFactory getFileStoreCursorStreamProviderFactory(FileStoreCursorStreamConfig config) {
    throw new UnsupportedOperationException("Only supported in EE edition");
  }
}
