/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.soap.internal.loader;

import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.mule.runtime.api.dsl.DslResolvingContext.getDefault;
import static org.mule.runtime.core.api.config.MuleManifest.getProductVersion;
import static org.mule.runtime.extension.api.annotation.Extension.DEFAULT_CONFIG_DESCRIPTION;
import static org.mule.runtime.extension.api.annotation.Extension.DEFAULT_CONFIG_NAME;
import static org.mule.runtime.extension.api.util.ExtensionMetadataTypeUtils.getId;
import static org.mule.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static org.mule.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader.VERSION;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.model.StringType;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.SubTypesModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.connection.ConnectionProviderModel;
import org.mule.runtime.extension.api.soap.MessageDispatcherProvider;
import org.mule.test.ram.DefaultPortalGunDispatcherProvider;
import org.mule.test.ram.MiniverseDispatcherProvider;
import org.mule.test.ram.RickAndMortyExtension;
import org.mule.test.ram.TestHttpMessageDispatcherProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class SoapExtensionWithCustomTransportsDeclarationTestCase extends AbstractSoapExtensionDeclarationTestCase {

  @Test
  public void assertSoapExtensionModel() {
    Map<String, Object> params = new HashMap<>();
    params.put(TYPE_PROPERTY_NAME, RickAndMortyExtension.class.getName());
    params.put(VERSION, getProductVersion());
    // TODO MULE-14517: This workaround should be replaced for a better and more complete mechanism
    params.put("COMPILATION_MODE", true);
    ExtensionModel model =
        loader.loadExtensionModel(RickAndMortyExtension.class.getClassLoader(), getDefault(emptySet()), params);

    assertThat(model.getConfigurationModels(), hasSize(1));
    ConfigurationModel configuration = model.getConfigurationModels().get(0);
    assertThat(configuration.getName(), is(DEFAULT_CONFIG_NAME));
    assertThat(configuration.getDescription(), is(DEFAULT_CONFIG_DESCRIPTION));
    assertThat(configuration.getOperationModels(), hasSize(1));
    assertSubtypes(model);
    List<ConnectionProviderModel> providers = configuration.getConnectionProviders();
    assertThat(providers, hasSize(1));
    assertConnectionProvider(providers.get(0), "rick-and-morty-connection", "", true,
                             new ParameterProber("wsdlUrl", StringType.class),
                             new ParameterProber("port", StringType.class),
                             new ParameterProber("service", StringType.class),
                             new ParameterProber("transport", ObjectType.class));
  }

  private void assertSubtypes(ExtensionModel model) {
    SubTypesModel subtypes = model.getSubTypes().iterator().next();
    assertThat(getId(subtypes.getBaseType()).get(), is(MessageDispatcherProvider.class.getName()));
    subtypes.getSubTypes()
        .forEach(subtype -> assertThat(getId(subtype).get(), isOneOf(TestHttpMessageDispatcherProvider.class.getName(),
                                                                     DefaultPortalGunDispatcherProvider.class.getName(),
                                                                     MiniverseDispatcherProvider.class.getName())));
  }
}
