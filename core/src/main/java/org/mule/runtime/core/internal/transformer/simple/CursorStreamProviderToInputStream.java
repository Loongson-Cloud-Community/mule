/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static org.mule.runtime.api.metadata.DataType.CURSOR_STREAM_PROVIDER;
import static org.mule.runtime.api.metadata.DataType.INPUT_STREAM;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.transformer.DiscoverableTransformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.transformer.AbstractTransformer;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Transforms a {@link CursorStreamProvider} to an {@link InputStream} by getting a cursor from it
 *
 * @since 4.0
 */
public class CursorStreamProviderToInputStream extends AbstractTransformer implements DiscoverableTransformer {

  private int priorityWeighting = DEFAULT_PRIORITY_WEIGHTING;

  public CursorStreamProviderToInputStream() {
    registerSourceType(CURSOR_STREAM_PROVIDER);
    setReturnDataType(INPUT_STREAM);
  }

  @Override
  protected Object doTransform(Object src, Charset enc) throws TransformerException {
    return ((CursorStreamProvider) src).openCursor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPriorityWeighting() {
    return priorityWeighting;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPriorityWeighting(int weighting) {
    priorityWeighting = weighting;
  }

}
