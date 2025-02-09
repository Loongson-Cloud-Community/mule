/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.api.metadata.DataType.CURSOR_STREAM_PROVIDER;
import static org.mule.runtime.api.metadata.DataType.INPUT_STREAM;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.transformer.DiscoverableTransformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.transformer.AbstractTransformer;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Transforms {@link InputStream input streams} into byte arrays. Streams are accepted in either its natural {@link InputStream}
 * form or as a {@link CursorStreamProvider}
 *
 * @since 4.0
 */
public class InputStreamToByteArray extends AbstractTransformer implements DiscoverableTransformer {

  private int priorityWeighting = MAX_PRIORITY_WEIGHTING;
  private ObjectToByteArray delegate = new ObjectToByteArray();

  public InputStreamToByteArray() {
    registerSourceType(CURSOR_STREAM_PROVIDER);
    registerSourceType(INPUT_STREAM);
    setReturnDataType(BYTE_ARRAY);
  }

  @Override
  protected Object doTransform(Object src, Charset enc) throws TransformerException {
    return delegate.doTransform(src, enc);
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
