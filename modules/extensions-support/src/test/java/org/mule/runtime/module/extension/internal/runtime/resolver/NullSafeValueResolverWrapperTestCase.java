/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static java.util.Optional.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.el.BindingContextUtils.getTargetBindingContext;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.toMetadataType;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.el.CompiledExpression;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.el.ExpressionManagerSession;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.size.SmallTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class NullSafeValueResolverWrapperTestCase extends AbstractMuleContextTestCase {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS, lenient = true)
  private CoreEvent event;

  @Mock
  private ObjectTypeParametersResolver objectTypeParametersResolver;

  @Mock
  private ExpressionManager expressionManager;

  private final ReflectionCache reflectionCache = new ReflectionCache();

  @Before
  public void setUp() {
    when(event.getError()).thenReturn(empty());
    when(event.getAuthentication()).thenReturn(empty());
    Message msg = of(null);
    when(event.getMessage()).thenReturn(msg);
    when(event.asBindingContext()).thenReturn(getTargetBindingContext(msg));
    when(event.getItemSequenceInfo()).thenReturn(empty());
  }

  @Test
  public void testMapType() throws Exception {
    assertExpected(new StaticValueResolver(null), toMetadataType(HashMap.class), false, new HashMap<>());
  }

  @Test
  public void testPojoType() throws Exception {
    ExpressionManagerSession session = mock(ExpressionManagerSession.class);
    when(session.evaluate(any(CompiledExpression.class), any(DataType.class)))
        .thenAnswer(inv -> new TypedValue<>(5, inv.getArgument(1)));
    when(expressionManager.openSession(any())).thenReturn(session);

    assertExpected(new StaticValueResolver(null), toMetadataType(DynamicPojo.class), true, new DynamicPojo(5));

    verify(event, times(1)).asBindingContext();
  }

  @Test
  public void testPojoWithStaticDefaultValue() throws Exception {
    assertExpected(new StaticValueResolver(null), toMetadataType(NonDynamicPojo.class), false, new NonDynamicPojo(false));
  }

  @Test
  public void testPojoWithMap() throws Exception {
    DynamicPojoWithMap pojo = new DynamicPojoWithMap();
    pojo.setMap(new HashMap<>());
    assertExpected(new StaticValueResolver(null), toMetadataType(DynamicPojoWithMap.class), false, pojo);
  }

  @Test
  public void testNullSafeSdkAndLegacyAnnotation() throws Exception {
    PojoUsingSdkApiAndLegacyApi pojo = new PojoUsingSdkApiAndLegacyApi();
    pojo.setParameters(new ASimplePojo(), new ASimplePojo());
    assertExpected(new StaticValueResolver(null), toMetadataType(PojoUsingSdkApiAndLegacyApi.class), false, pojo);
  }

  private void assertExpected(ValueResolver valueResolver, MetadataType type, boolean isDynamic, Object expected)
      throws Exception {
    ValueResolver resolver = NullSafeValueResolverWrapper.of(valueResolver, type, reflectionCache, expressionManager,
                                                             muleContext, objectTypeParametersResolver);
    ValueResolvingContext ctx = ValueResolvingContext.builder(event)
        .withExpressionManager(expressionManager)
        .build();
    assertThat(resolver.isDynamic(), is(isDynamic));
    assertThat(resolver.resolve(ctx), is(expected));
  }

  public static class DynamicPojo {

    public DynamicPojo() {}

    public DynamicPojo(int time) {
      this.time = time;
    }

    @Parameter
    @Optional(defaultValue = "#[5]")
    private int time;

    public int getTime() {
      return time;
    }

    public void setTime(int time) {
      this.time = time;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof DynamicPojo) {
        DynamicPojo that = (DynamicPojo) o;
        return time == that.time;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(time);
    }
  }


  public static class DynamicPojoWithMap {

    public DynamicPojoWithMap() {}

    @Parameter
    @Optional
    @NullSafe
    private Map<String, String> map;

    public Map<String, String> getMap() {
      return map;
    }

    public void setMap(Map<String, String> map) {
      this.map = map;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof DynamicPojoWithMap) {
        DynamicPojoWithMap that = (DynamicPojoWithMap) o;
        return Objects.equals(map, that.map);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(map);
    }
  }


  public static class NonDynamicPojo {

    public NonDynamicPojo() {}

    public NonDynamicPojo(Boolean staticDefaultValue) {
      this.staticDefaultValue = staticDefaultValue;
    }

    @Parameter
    @Optional(defaultValue = "false")
    private Boolean staticDefaultValue;

    public Boolean getStaticDefaultValue() {
      return staticDefaultValue;
    }

    public void setStaticDefaultValue(Boolean staticDefaultValue) {
      this.staticDefaultValue = staticDefaultValue;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof NonDynamicPojo) {
        NonDynamicPojo that = (NonDynamicPojo) o;
        return Objects.equals(staticDefaultValue, that.staticDefaultValue);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(staticDefaultValue);
    }
  }

  public static class PojoUsingSdkApiAndLegacyApi {

    public PojoUsingSdkApiAndLegacyApi() {}

    @Parameter
    @Optional
    @NullSafe
    private ASimplePojo parameter1;

    @Parameter
    @Optional
    @org.mule.sdk.api.annotation.param.NullSafe
    private ASimplePojo parameter2;

    public void setParameters(ASimplePojo parameter1, ASimplePojo parameter2) {
      this.parameter1 = parameter1;
      this.parameter2 = parameter2;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof PojoUsingSdkApiAndLegacyApi) {
        PojoUsingSdkApiAndLegacyApi that = (PojoUsingSdkApiAndLegacyApi) o;
        return Objects.equals(parameter1, that.parameter1) && Objects.equals(parameter2, that.parameter2);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(parameter1, parameter2);
    }
  }

  public static class ASimplePojo {

    public String parameter = "parameter";

    public ASimplePojo() {}

    @Override
    public boolean equals(Object o) {
      if (o instanceof ASimplePojo) {
        ASimplePojo that = (ASimplePojo) o;
        return Objects.equals(this.parameter, that.parameter);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(parameter);
    }
  }
}
