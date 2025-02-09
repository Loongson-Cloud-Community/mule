/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.values.extension.config;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.test.values.extension.ValuesOperations;
import org.mule.test.values.extension.connection.ConnectionWithFailureErrorProvider;
import org.mule.test.values.extension.connection.ConnectionWithValueFourBoundActingParameters;
import org.mule.test.values.extension.connection.ConnectionWithParameterWithFieldValues;
import org.mule.test.values.extension.connection.ConnectionWithValueParameter;
import org.mule.test.values.extension.connection.ConnectionWithValueWithRequiredParam;
import org.mule.test.values.extension.connection.ConnectionWithValuesWithRequiredParamsFromParamGroup;
import org.mule.test.values.extension.connection.ConnectionWithValuesWithRequiredParamsFromShowInDslGroup;
import org.mule.test.values.extension.connection.ValuesConnectionProvider;
import org.mule.test.values.extension.source.SimpleSourceWithParameterWithFieldValues;
import org.mule.test.values.extension.source.SourceMustNotStart;
import org.mule.test.values.extension.source.SourceWithConfiguration;
import org.mule.test.values.extension.source.SourceWithConnection;
import org.mule.test.values.extension.source.SourceWithMultiLevelFieldValues;
import org.mule.test.values.extension.source.SourceWithMultiLevelValue;
import org.mule.test.values.extension.source.SourceWithRequiredParameterInsideShowInDslGroup;
import org.mule.test.values.extension.source.SourceWithRequiredParameterWithAlias;
import org.mule.test.values.extension.source.SourceWithValuesWithRequiredParameterInsideParamGroup;
import org.mule.test.values.extension.source.SourceWithValuesWithRequiredParameters;

import java.util.List;

@Configuration
@ConnectionProviders({ValuesConnectionProvider.class, ConnectionWithValueParameter.class,
    ConnectionWithValueWithRequiredParam.class, ConnectionWithValuesWithRequiredParamsFromParamGroup.class,
    ConnectionWithValuesWithRequiredParamsFromShowInDslGroup.class, ConnectionWithFailureErrorProvider.class,
    ConnectionWithValueFourBoundActingParameters.class, ConnectionWithParameterWithFieldValues.class})
@Operations({ValuesOperations.class})
@Sources({SourceWithConfiguration.class, SourceWithConnection.class, SourceWithValuesWithRequiredParameters.class,
    SourceWithValuesWithRequiredParameterInsideParamGroup.class})
@org.mule.sdk.api.annotation.Sources({SourceWithRequiredParameterWithAlias.class,
    SourceWithRequiredParameterInsideShowInDslGroup.class,
    SourceWithMultiLevelValue.class, SourceMustNotStart.class, SimpleSourceWithParameterWithFieldValues.class,
    SourceWithMultiLevelFieldValues.class})
public class SimpleConfig {

  @Parameter
  @Optional(defaultValue = "noExpression")
  private String data;

  @Parameter
  @Optional
  private List<String> configValues;

  public String getData() {
    return data;
  }

  public List<String> getConfigValues() {
    return configValues;
  }
}
