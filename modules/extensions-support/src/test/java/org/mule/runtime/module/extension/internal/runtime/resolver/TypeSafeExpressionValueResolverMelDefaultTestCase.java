/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_EXPRESSION_LANGUAGE;
import static org.mule.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.toDataType;
import static org.mule.tck.util.MuleContextUtils.eventBuilder;
import static org.mule.test.allure.AllureConstants.ExpressionLanguageFeature.EXPRESSION_LANGUAGE;
import static org.mule.test.allure.AllureConstants.ExpressionLanguageFeature.ExpressionLanguageStory.SUPPORT_MVEL_DW;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.java.api.JavaTypeLoader;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.core.api.el.ExtendedExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.runtime.core.internal.el.DefaultExpressionManager;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.verification.VerificationMode;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(EXPRESSION_LANGUAGE)
@Story(SUPPORT_MVEL_DW)
public class TypeSafeExpressionValueResolverMelDefaultTestCase extends AbstractMuleContextTestCase {

  private static final String HELLO_WORLD = "Hello World!";
  private static final MetadataType STRING =
      new JavaTypeLoader(Thread.currentThread().getContextClassLoader()).load(String.class);

  @Rule
  public SystemProperty melDefault = new SystemProperty("mule.test.mel.default", "true");

  @Rule
  public ExpectedException expected = none();

  private ExtendedExpressionManager expressionManager;

  @Override
  protected void doSetUp() throws Exception {
    muleContext = spy(muleContext);
    expressionManager = spy(muleContext.getExpressionManager());

    ((DefaultExpressionManager) expressionManager).setMelDefault(true);
    ((DefaultExpressionManager) expressionManager)
        .setExpressionLanguage((((MuleContextWithRegistry) muleContext).getRegistry()).lookupObject(OBJECT_EXPRESSION_LANGUAGE));

    when(muleContext.getExpressionManager()).thenReturn(expressionManager);
    ((MuleContextWithRegistry) muleContext).getRegistry().registerObject("_muleExpressionManager", expressionManager);
  }

  @Test
  public void expressionLanguageWithoutTransformation() throws Exception {
    ValueResolvingContext context = buildContext(eventBuilder(muleContext).message(of("World!")).build());
    assertResolved(getResolver("#['Hello ' + payload]", STRING).resolve(context), HELLO_WORLD, never());
  }

  @Test
  public void expressionTemplateWithoutTransformation() throws Exception {
    ValueResolvingContext context = buildContext(eventBuilder(muleContext).message(of("World!")).build());
    assertResolved(getResolver("Hello #[payload]", STRING).resolve(context), HELLO_WORLD, times(1));
  }

  @Test
  public void constant() throws Exception {
    ValueResolvingContext context = buildContext(eventBuilder(muleContext).message(of(HELLO_WORLD)).build());
    assertResolved(getResolver("Hello World!", STRING).resolve(context), HELLO_WORLD, never());
  }

  @Test
  public void expressionWithTransformation() throws Exception {
    ValueResolvingContext context = buildContext(eventBuilder(muleContext).message(of(HELLO_WORLD)).build());
    assertResolved(getResolver("#[true]", STRING).resolve(context), "true", never());
  }

  @Test
  public void templateWithTransformation() throws Exception {
    ValueResolvingContext context = buildContext(eventBuilder(muleContext).message(of(HELLO_WORLD)).build());
    assertResolved(getResolver("tru#['e']", STRING).resolve(context), "true", times(1));
  }

  private ValueResolvingContext buildContext(CoreEvent event) {
    return ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build();
  }

  @Test
  public void nullExpression() throws Exception {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("Expression cannot be blank or null");
    getResolver(null, STRING);
  }

  @Test
  public void blankExpression() throws Exception {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("Expression cannot be blank or null");
    getResolver(EMPTY, STRING);
  }

  @Test
  public void nullExpectedType() throws Exception {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("expected type cannot be null");
    getResolver("#[payload]", null);
  }

  private void assertResolved(Object resolvedValue, Object expected, VerificationMode expressionManagerVerificationMode) {
    assertThat(resolvedValue, instanceOf(String.class));
    assertThat(resolvedValue, equalTo(expected));
    verifyExpressionManager(expressionManagerVerificationMode);
  }

  private void verifyExpressionManager(VerificationMode mode) {
    verify(expressionManager, mode).parse(anyString(), any(CoreEvent.class), nullable(ComponentLocation.class));
  }

  private <T> ValueResolver<T> getResolver(String expression, MetadataType expectedType) throws Exception {
    TypeSafeExpressionValueResolver<T> valueResolver = new TypeSafeExpressionValueResolver(expression,
                                                                                           getType(expectedType).orElse(null),
                                                                                           toDataType(expectedType));
    muleContext.getInjector().inject(valueResolver);
    valueResolver.setExtendedExpressionManager(expressionManager);
    valueResolver.setTransformationService(muleContext.getTransformationService());
    valueResolver.initialise();
    return valueResolver;
  }
}
