/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.tck.testmodels.fruit.Apple;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.reflections.ReflectionUtils;

@SmallTest
public class FieldSetterTestCase extends AbstractMuleTestCase {

  private FieldSetter<Apple, Boolean> setter;
  private Apple apple = new Apple(false);
  private Field field;

  @Before
  public void before() throws Exception {
    field = ReflectionUtils.getAllFields(Apple.class, ReflectionUtils.withName("bitten")).iterator().next();
    setter = new FieldSetter<>(field);
  }

  @Test
  public void set() throws Exception {
    setter.set(apple, true);
    assertThat(apple.isBitten(), is(true));
  }

  @Test
  public void getField() {
    assertThat(setter.getField(), is(sameInstance(field)));
  }
}
