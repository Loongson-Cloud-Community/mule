/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.deployment.model.api.artifact.extension;

import static org.mule.runtime.extension.api.ExtensionConstants.ALL_SUPPORTED_JAVA_VERSIONS;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.core.api.extension.provider.RuntimeExtensionModelProvider;

// TODO W-10928152: remove this class when migrating to use the new extension model loading API.
public class TestRuntimeExtensionModelProvider implements RuntimeExtensionModelProvider {

  @Override
  public ExtensionModel createExtensionModel() {
    ExtensionModel extModel = mock(ExtensionModel.class);
    when(extModel.getName()).thenReturn("testRuntime");
    when(extModel.getSupportedJavaVersions()).thenReturn(ALL_SUPPORTED_JAVA_VERSIONS);
    XmlDslModel xmlDslModel = XmlDslModel.builder().setPrefix("test-runtime").build();
    when(extModel.getXmlDslModel()).thenReturn(xmlDslModel);
    return extModel;
  }

}
