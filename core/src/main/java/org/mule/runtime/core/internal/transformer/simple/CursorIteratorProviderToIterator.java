/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static org.mule.runtime.api.metadata.DataType.CURSOR_ITERATOR_PROVIDER;
import static org.mule.runtime.api.metadata.DataType.ITERATOR;
import org.mule.runtime.api.streaming.object.CursorIteratorProvider;
import org.mule.runtime.core.api.transformer.DiscoverableTransformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.transformer.AbstractTransformer;

import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Transforms a {@link CursorIteratorProvider} to an {@link Iterator} by getting a cursor from it
 *
 * @since 4.0
 */
public class CursorIteratorProviderToIterator extends AbstractTransformer implements DiscoverableTransformer {

  private int priorityWeighting = DEFAULT_PRIORITY_WEIGHTING;

  public CursorIteratorProviderToIterator() {
    registerSourceType(CURSOR_ITERATOR_PROVIDER);
    setReturnDataType(ITERATOR);
  }

  @Override
  protected Object doTransform(Object src, Charset enc) throws TransformerException {
    return ((CursorIteratorProvider) src).openCursor();
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
