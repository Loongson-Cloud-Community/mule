/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static org.mule.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static org.mule.test.allure.AllureConstants.RegistryFeature.TransfromersStory.TRANSFORMERS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.runtime.core.privileged.registry.RegistrationException;
import org.mule.runtime.core.privileged.transformer.TransformersRegistry;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(REGISTRY)
@Story(TRANSFORMERS)
public class BasicTypeAutoTransformationTestCase extends AbstractMuleContextTestCase {

  private TransformersRegistry registry;

  @Before
  public void before() throws RegistrationException {
    registry = ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(TransformersRegistry.class);
  }

  @Test
  public void testTypes() throws TransformerException {
    testType("1", Integer.class, Integer.TYPE, Integer.valueOf(1));
    testType("1", Long.class, Long.TYPE, Long.valueOf(1));
    testType("1", Short.class, Short.TYPE, Short.valueOf((short) 1));
    testType("1.1", Double.class, Double.TYPE, Double.valueOf(1.1));
    testType("1.1", Float.class, Float.TYPE, Float.valueOf((float) 1.1));
    testType("1.1", BigDecimal.class, null, BigDecimal.valueOf(1.1));
    testType("true", Boolean.class, Boolean.TYPE, Boolean.TRUE);
  }

  protected void testType(String string, Class type, Class primitive, Object value) throws TransformerException {
    assertThat(lookupFromStringTransformer(type).transform(string), is(value));
    assertThat(lookupToStringTransformer(type).transform(value), is(string));
    if (primitive != null) {
      assertThat(lookupFromStringTransformer(primitive).transform(string), is(value));
      assertThat(lookupToStringTransformer(primitive).transform(value), is(string));
    }
  }

  private Transformer lookupFromStringTransformer(Class to) throws TransformerException {
    return registry.lookupTransformer(DataType.STRING, DataType.fromType(to));
  }

  private Transformer lookupToStringTransformer(Class from) throws TransformerException {
    return registry.lookupTransformer(DataType.fromType(from), DataType.STRING);
  }

}
