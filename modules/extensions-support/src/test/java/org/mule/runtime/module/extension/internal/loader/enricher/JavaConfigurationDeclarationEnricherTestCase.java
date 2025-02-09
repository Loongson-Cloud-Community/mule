/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.enricher;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.runtime.api.dsl.DslResolvingContext.getDefault;
import static org.mule.runtime.core.api.config.MuleManifest.getProductVersion;
import static org.mule.runtime.module.extension.internal.loader.enricher.EnricherTestUtils.checkIsPresent;
import static org.mule.runtime.module.extension.internal.loader.enricher.EnricherTestUtils.getDeclaration;
import static org.mule.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import org.mule.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import org.mule.runtime.module.extension.internal.loader.java.enricher.JavaConfigurationDeclarationEnricher;
import org.mule.runtime.module.extension.internal.loader.java.property.ConfigTypeModelProperty;
import org.mule.test.heisenberg.extension.HeisenbergExtension;

import org.junit.Before;
import org.junit.Test;

public class JavaConfigurationDeclarationEnricherTestCase {

  private static final String GET_ENEMY = "getEnemy";
  private static final String LISTEN_PAYMENTS = "ListenPayments";
  private ExtensionDeclaration declaration = null;

  @Before
  public void setUp() {
    ExtensionDeclarer declarer = declarerFor(HeisenbergExtension.class, getProductVersion());
    new JavaConfigurationDeclarationEnricher()
        .enrich(new DefaultExtensionLoadingContext(declarer, getClass().getClassLoader(), getDefault(emptySet())));
    declaration = declarer.getDeclaration();
  }

  @Test
  public void verifyConfigurationModelPropertyOnOperation() {
    OperationDeclaration operationDeclaration = getDeclaration(declaration.getConfigurations().get(0).getOperations(), GET_ENEMY);
    final ConfigTypeModelProperty configTypeModelProperty = checkIsPresent(operationDeclaration, ConfigTypeModelProperty.class);

    assertType(configTypeModelProperty);
  }

  @Test
  public void verifyConfigurationModelPropertyOnSource() {
    SourceDeclaration sourceDeclaration =
        getDeclaration(declaration.getConfigurations().get(0).getMessageSources(), LISTEN_PAYMENTS);
    final ConfigTypeModelProperty configTypeModelProperty = checkIsPresent(sourceDeclaration, ConfigTypeModelProperty.class);

    assertType(configTypeModelProperty);
  }

  private void assertType(ConfigTypeModelProperty configTypeModelProperty) {
    assertThat(configTypeModelProperty.getConfigType(), equalTo(HeisenbergExtension.class));
  }
}
