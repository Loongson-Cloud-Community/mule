/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.execution;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.getParameter;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.getResolver;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.mockConfigurationInstance;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import org.mule.runtime.module.extension.internal.runtime.config.ConfigurationObjectBuilder;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolvingContext;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationObjectBuilderTestCase extends AbstractMuleTestCase {

  private static final String NAME_VALUE = "name";
  private static final String DESCRIPTION_VALUE = "description";

  public static final ParameterModel nameParameterModel = getParameter("name", String.class);
  public static final ParameterModel descriptionParameterModel = getParameter("description", String.class);

  public static ResolverSet createResolverSet() throws Exception {
    ResolverSet resolverSet = new ResolverSet(mock(MuleContext.class));
    resolverSet.add(nameParameterModel.getName(), getResolver(NAME_VALUE));
    resolverSet.add(descriptionParameterModel.getName(), getResolver(DESCRIPTION_VALUE));

    return resolverSet;
  }

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ConfigurationModel configurationModel;

  @Mock
  private CoreEvent event;

  @Mock
  private ExpressionManager expressionManager;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private MuleContext context;

  private ConfigurationObjectBuilder<TestConfig> configurationObjectBuilder;
  private ResolverSet resolverSet;


  @Before
  public void before() throws Exception {
    when(configurationModel.getAllParameterModels()).thenReturn(asList(nameParameterModel, descriptionParameterModel));
    mockConfigurationInstance(configurationModel, new TestConfig());
    when(configurationModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(Optional.empty());

    resolverSet = createResolverSet();

    configurationObjectBuilder = new ConfigurationObjectBuilder<>(configurationModel, resolverSet, expressionManager, context);
  }

  @Test
  public void build() throws MuleException {
    TestConfig testConfig = configurationObjectBuilder.build(ValueResolvingContext.builder(event)
        .withExpressionManager(expressionManager)
        .build()).getFirst();
    assertThat(testConfig.getName(), is(NAME_VALUE));
    assertThat(testConfig.getDescription(), is(DESCRIPTION_VALUE));
  }

  public static class TestConfig {

    private String name;
    private String description;

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }
  }
}
