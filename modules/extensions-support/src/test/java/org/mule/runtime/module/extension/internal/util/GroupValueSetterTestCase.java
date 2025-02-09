/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.getField;

import org.mule.metadata.java.api.JavaTypeLoader;

import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.module.extension.internal.loader.ParameterGroupDescriptor;
import org.mule.runtime.module.extension.internal.loader.java.type.runtime.TypeWrapper;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.test.heisenberg.extension.HeisenbergExtension;
import org.mule.test.heisenberg.extension.model.PersonalInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class GroupValueSetterTestCase extends AbstractMuleTestCase {

  private static final String NAME = "name";
  private static final Integer AGE = 50;
  private static final Date DATE = new Date();

  private ValueSetter valueSetter;

  @Mock
  private ExpressionManager expressionManager;

  @Mock
  private ResolverSetResult result;

  private ReflectionCache reflectionCache = new ReflectionCache();

  @Before
  public void before() throws Exception {
    final String personalInfo = "personalInfo";
    ParameterGroupDescriptor group =
        new ParameterGroupDescriptor("group", new TypeWrapper(PersonalInfo.class,
                                                              new JavaTypeLoader(Thread.currentThread().getContextClassLoader())),
                                     ExtensionsTypeLoaderFactory.getDefault().createTypeLoader().load(PersonalInfo.class),
                                     getField(HeisenbergExtension.class, personalInfo, reflectionCache).get(), null);

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("name", NAME);
    resultMap.put("age", AGE);
    resultMap.put("dateOfBirth", DATE);

    when(result.asMap()).thenReturn(resultMap);

    valueSetter = new GroupValueSetter(group, () -> reflectionCache, () -> expressionManager);
  }

  @Test
  public void set() throws Exception {
    HeisenbergExtension extension = new HeisenbergExtension();
    valueSetter.set(extension, result);

    assertThat(extension.getPersonalInfo().getName(), is(NAME));
    assertThat(extension.getPersonalInfo().getAge(), is(AGE));
    assertThat(extension.getPersonalInfo().getDateOfBirth(), is(sameInstance(DATE)));
  }
}
