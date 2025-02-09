/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.transformer.DiscoverableTransformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.transformer.AbstractTransformer;

import java.nio.charset.Charset;
import java.text.NumberFormat;

/**
 * <code>NumberToString</code> converts a Number to a String. A NumberFormat is used if one is provided.
 */
public class NumberToString extends AbstractTransformer implements DiscoverableTransformer {

  private NumberFormat numberFormat;

  /**
   * Give core transformers a slighty higher priority
   */
  private int priorityWeighting = DiscoverableTransformer.DEFAULT_PRIORITY_WEIGHTING + 1;

  public NumberToString() {
    registerSourceType(DataType.NUMBER);
    setReturnDataType(DataType.STRING);
  }

  public NumberToString(NumberFormat numberFormat) {
    this();
    this.numberFormat = numberFormat;
  }

  @Override
  public Object doTransform(Object src, Charset encoding) throws TransformerException {
    if (src == null) {
      return "";
    }

    if (numberFormat != null) {
      return numberFormat.format(src);
    } else {
      return src.toString();
    }
  }

  @Override
  public int getPriorityWeighting() {
    return priorityWeighting;
  }

  @Override
  public void setPriorityWeighting(int priorityWeighting) {
    this.priorityWeighting = priorityWeighting;
  }

}
