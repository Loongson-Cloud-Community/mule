/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.graph;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.transformer.Converter;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeConverterFilter implements ConverterFilter {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private final ConverterFilter[] filters;

  public CompositeConverterFilter(ConverterFilter... filters) {
    this.filters = filters;
  }

  @Override
  public List<Converter> filter(List<Converter> converters, DataType source, DataType result) {
    List<Converter> filteredTransformers = new LinkedList<>(converters);

    for (ConverterFilter filter : filters) {
      if (filteredTransformers.size() <= 1) {
        break;
      }

      filteredTransformers = filter.filter(filteredTransformers, source, result);
    }

    return filteredTransformers;
  }
}
