/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.complex.config.properties.deprecated.extension;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;

import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;

import java.util.List;
import java.util.Optional;

public class ComplexConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

  private final List<String> textsFromComplexParams;

  public ComplexConfigurationPropertiesProvider(List<String> textsFromComplexParams) {
    this.textsFromComplexParams = textsFromComplexParams;
  }

  @Override
  public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {
    if (textsFromComplexParams.isEmpty()) {
      return empty();
    }

    return of(new ConfigurationProperty() {

      @Override
      public Object getSource() {
        return "ComplexConfigurationPropertiesProvider";
      }

      @Override
      public Object getRawValue() {
        return textsFromComplexParams.stream().collect(joining(","));
      }

      @Override
      public String getKey() {
        return "textsFromComplexParams";
      }
    });
  }

  @Override
  public String getDescription() {
    return "ComplexConfigurationPropertiesProvider";
  }

}
