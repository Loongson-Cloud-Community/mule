/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.transformer.DataTypeConversionResolver;
import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.privileged.transformer.TransformersRegistry;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves data type conversion finding an appropriate converter that is able to execute the required transformation. The lookup
 * is executed dynamically using the discovering of transformers using the application's {@link MuleContext}
 */
public class DynamicDataTypeConversionResolver implements DataTypeConversionResolver {

  private static final Logger logger = LoggerFactory.getLogger(DynamicDataTypeConversionResolver.class);

  private final TransformersRegistry transformersRegistry;

  @Inject
  public DynamicDataTypeConversionResolver(TransformersRegistry transformersRegistry) {
    this.transformersRegistry = transformersRegistry;
  }

  @Override
  public Transformer resolve(DataType sourceType, List<DataType> targetDataTypes) {
    Transformer transformer = null;

    for (DataType targetDataType : targetDataTypes) {
      try {
        transformer = transformersRegistry.lookupTransformer(sourceType, targetDataType);

        if (transformer != null) {
          break;
        }
      } catch (TransformerException e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Unable to find an implicit conversion from " + sourceType + " to " + targetDataType);
        }
      }
    }

    return transformer;
  }
}
