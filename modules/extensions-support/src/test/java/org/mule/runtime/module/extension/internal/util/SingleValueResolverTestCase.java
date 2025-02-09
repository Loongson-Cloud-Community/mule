/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.getField;

import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.test.heisenberg.extension.model.PersonalInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class SingleValueResolverTestCase extends AbstractMuleTestCase {

  private static final String NAME = "name";

  @Mock
  private ParameterModel parameterModel;

  @Mock
  private ResolverSetResult result;


  private ValueSetter valueSetter;

  @Before
  public void before() {
    when(result.get(NAME)).thenReturn(NAME);
    valueSetter = new SingleValueSetter(NAME, getField(PersonalInfo.class, "name", new ReflectionCache()).get());
  }

  @Test
  public void set() throws Exception {
    PersonalInfo info = new PersonalInfo();
    valueSetter.set(info, result);
    assertThat(info.getName(), is(NAME));
  }
}
