/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.HELLO_WORLD;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class RegistryLookupValueResolverTestCase extends AbstractMuleTestCase {

  private static final String KEY = "key";
  private static final String FAKE_KEY = "not there";

  @Mock(answer = RETURNS_DEEP_STUBS)
  private CoreEvent event;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private MuleContext muleContext;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private Registry registry;

  @Mock
  private ExpressionManager expressionManager;

  private ValueResolver resolver;

  @Before
  public void before() throws Exception {
    when(registry.lookupByName(KEY)).thenReturn(of(HELLO_WORLD));
    when(registry.lookupByName(FAKE_KEY)).thenReturn(empty());
    resolver = new RegistryLookupValueResolver(KEY);
    ((RegistryLookupValueResolver) resolver).setRegistry(registry);
  }

  @Test
  public void cache() throws Exception {
    ValueResolvingContext ctx = ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build();
    Object value = resolver.resolve(ctx);
    assertThat(value, is(HELLO_WORLD));
    verify(registry).lookupByName(KEY);
  }

  @Test
  public void isDynamic() {
    assertThat(resolver.isDynamic(), is(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullKey() throws MuleException {
    ValueResolvingContext ctx = ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build();
    new RegistryLookupValueResolver(null).resolve(ctx);
  }

  @Test(expected = IllegalArgumentException.class)
  public void blankKey() throws MuleException {
    ValueResolvingContext ctx = ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build();
    new RegistryLookupValueResolver("").resolve(ctx);
  }

  @Test(expected = ConfigurationException.class)
  public void nonExistingKey() throws Exception {
    RegistryLookupValueResolver<Object> valueResolver = new RegistryLookupValueResolver<>(FAKE_KEY);
    valueResolver.setRegistry(registry);
    ValueResolvingContext ctx = ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build();
    valueResolver.resolve(ctx);
  }
}
