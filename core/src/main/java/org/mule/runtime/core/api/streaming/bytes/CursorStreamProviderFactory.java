/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming.bytes;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.streaming.CursorProviderFactory;

import java.io.InputStream;

/**
 * Specialization of {@link CursorStreamProvider} which creates {@link CursorStreamProvider} instances out of {@link InputStream}
 * instances
 *
 * @since 4.0
 */
@NoImplement
public interface CursorStreamProviderFactory extends CursorProviderFactory<InputStream> {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if the value is an {@link InputStream}
   */
  @Override
  default boolean accepts(Object value) {
    return value instanceof InputStream;
  }
}
