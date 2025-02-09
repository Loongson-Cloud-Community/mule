/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_CONFIGURATION_PROPERTIES;

import org.mule.runtime.api.component.ConfigurationProperties;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.tck.size.SmallTest;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class RequiredParameterValueResolverWrapperTestCase extends LifecycleAwareValueResolverWrapperTestCase<Object> {

  private static final String PARAMETER_NAME = "requiredParam";
  private static final String EXPRESSION = "#[someExpression]";

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private ValueResolvingContext context;

  @Mock
  private ConfigurationProperties configProperties;

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(OBJECT_CONFIGURATION_PROPERTIES, configProperties);
  }

  @Before
  public void setUpConfigProperties() {
    when(configProperties.resolveBooleanProperty(anyString())).thenReturn(empty());
  }

  @Override
  protected LifecycleAwareValueResolverWrapper<Object> createWrapper(ValueResolver<Object> delegate) {
    return new RequiredParameterValueResolverWrapper<>(delegate, PARAMETER_NAME, EXPRESSION);
  }

  @Override
  protected ValueResolver<Object> createDelegate() {
    return mock(ValueResolver.class, withSettings().extraInterfaces(Lifecycle.class));
  }

  @Test
  public void nonNullValue() throws Exception {
    Object value = new Object();
    when(delegate.resolve(context)).thenReturn(value);

    assertThat(wrapper.resolve(context), is(sameInstance(value)));
  }

  @Test
  public void resolveNullValue() throws Exception {
    // This cannot be validated at the AST because the exception results of a provided expression evaluating to null
    when(delegate.resolve(context)).thenReturn(null);
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(allOf(containsString(PARAMETER_NAME), containsString(EXPRESSION)));

    wrapper.resolve(context);
  }
}
