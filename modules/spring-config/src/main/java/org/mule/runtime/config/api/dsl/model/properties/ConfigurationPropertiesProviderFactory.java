/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.api.dsl.model.properties;

import static org.mule.runtime.config.internal.dsl.model.properties.ConfigurationPropertiesProviderFactoryUtils.resolveConfigurationParameters;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.internal.dsl.model.DefaultConfigurationParameters;
import org.mule.runtime.properties.api.ConfigurationPropertiesProvider;
import org.mule.runtime.properties.api.ResourceProvider;

import java.util.function.UnaryOperator;

/**
 * Builds the provider for a custom configuration properties element.
 *
 * @since 4.1
 *
 * @deprecated since 4.4, use org.mule.runtime.properties.api.ConfigurationPropertiesProviderFactory instead.
 */
@Deprecated
public interface ConfigurationPropertiesProviderFactory
    extends org.mule.runtime.properties.api.ConfigurationPropertiesProviderFactory {

  /**
   * @return the component identifier of the supported element.
   */
  @Override
  ComponentIdentifier getSupportedComponentIdentifier();

  /**
   * Builds a properties provider for each matching configuration element.
   *
   * @param parameters               the configuration parameters, after resolving property placeholders
   * @param externalResourceProvider the resource provider for locating files (such as .properties and .yaml)
   * @return the properties provider
   */
  default ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                         ResourceProvider externalResourceProvider) {
    return createProvider(parameters, uri -> externalResourceProvider.getResourceAsStream(uri));
  }

  /**
   * Builds a properties provider for the provided {@code providerElementDeclaration}.
   *
   * @param providerElementDeclaration the configuration parameters, after resolving property placeholders
   * @param localResolver              the resolver of property placeholders found in the provided declaration
   * @param externalResourceProvider   the resource provider for locating files (such as .properties and .yaml)
   * @return the properties provider
   */
  @Override
  default ConfigurationPropertiesProvider createProvider(ComponentAst providerElementDeclaration,
                                                         UnaryOperator<String> localResolver,
                                                         ResourceProvider externalResourceProvider) {
    DefaultConfigurationParameters.Builder configurationParametersBuilder = DefaultConfigurationParameters.builder();
    ConfigurationParameters configurationParameters =
        resolveConfigurationParameters(configurationParametersBuilder, providerElementDeclaration, localResolver);

    return createProvider(configurationParameters, uri -> externalResourceProvider.getResourceAsStream(uri));
  }

  /**
   * Builds a properties provider for each matching configuration element.
   *
   * @param parameters               the configuration parameters, after resolving property placeholders
   * @param externalResourceProvider the resource provider for locating files (such as .properties and .yaml)
   * @return the properties provider
   */
  org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                                                                  org.mule.runtime.config.api.dsl.model.ResourceProvider externalResourceProvider);
}
