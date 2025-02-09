/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.privileged.dsl.processor;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mule.runtime.api.metadata.MediaType.parseDefinedInApp;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.DataTypeParamsBuilder;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.config.api.dsl.ObjectFactoryCommonConfigurator;
import org.mule.runtime.core.privileged.processor.simple.AbstractAddVariablePropertyProcessor;

import java.util.Map;

/**
 * {@link ObjectFactoryCommonConfigurator} for transformers in Mule.
 *
 * The transformer class that will be configured setting the returnType, mimeType, name, encoding and ignoreBadInput
 * configuration.
 *
 * @since 4.0
 */
public final class AddVariablePropertyConfigurator
    implements ObjectFactoryCommonConfigurator<AbstractAddVariablePropertyProcessor> {

  /**
   * Configures the common parameters of every transformer.
   *
   * @param propVarSetterInstance the transformar instance
   * @param parameters            the set of parameters configured in the component model according to the
   *                              {@link org.mule.runtime.dsl.api.component.ComponentBuildingDefinition}
   */
  @Override
  public void configure(AbstractAddVariablePropertyProcessor propVarSetterInstance, Map<String, Object> parameters) {
    String mimeType = (String) parameters.get("mimeType");
    String encoding = (String) parameters.get("encoding");
    if (mimeType != null) {
      DataTypeParamsBuilder builder = DataType.builder();
      if (isNotEmpty(mimeType)) {
        builder.mediaType(parseDefinedInApp(mimeType));
      }
      propVarSetterInstance.setReturnDataType(builder.charset(encoding).build());
    }
  }

}
