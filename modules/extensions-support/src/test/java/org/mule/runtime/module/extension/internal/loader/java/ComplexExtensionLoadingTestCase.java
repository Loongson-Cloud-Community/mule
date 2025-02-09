/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mule.test.vegan.extension.VeganExtension.APPLE;
import static org.mule.test.vegan.extension.VeganExtension.BANANA;
import static org.mule.test.vegan.extension.VeganExtension.KIWI;
import static org.mule.test.vegan.extension.VeganExtension.VEGAN;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import org.mule.tck.size.SmallTest;
import org.mule.test.vegan.extension.VeganExtension;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class ComplexExtensionLoadingTestCase extends AbstractJavaExtensionDeclarationTestCase {

  private ExtensionDeclaration extensionDeclaration;

  @Before
  public void setUp() {
    setDeclarer(declarerFor(VeganExtension.class));
    extensionDeclaration = declareExtension().getDeclaration();
  }

  @Test
  public void extension() {
    assertThat(extensionDeclaration.getName(), is(VEGAN));
    assertThat(extensionDeclaration.getConfigurations(), hasSize(6));
    assertOperation(APPLE, "eatApple");
    assertOperation(BANANA, "eatBanana");
    assertOperation(KIWI, "eatKiwi");
  }

  private void assertOperation(String configName, String operationName) {
    ConfigurationDeclaration config =
        extensionDeclaration.getConfigurations().stream().filter(c -> c.getName().equals(configName)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No config with name " + configName));

    OperationDeclaration operation = config.getOperations().stream().filter(model -> model.getName().equals(operationName))
        .findFirst().orElseThrow(() -> new IllegalArgumentException("No operation with name " + operationName));

    assertThat(operation.getName(), is(operationName));
  }
}
