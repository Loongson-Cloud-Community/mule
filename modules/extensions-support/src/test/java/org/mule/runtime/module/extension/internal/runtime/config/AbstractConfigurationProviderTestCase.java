/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_TIME_SUPPLIER;
import static org.mule.tck.MuleTestUtils.spyInjector;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.module.extension.internal.runtime.DefaultExecutionContext;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.tck.util.TestTimeSupplier;

import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractConfigurationProviderTestCase<T> extends AbstractMuleContextTestCase {

  protected static final String CONFIG_NAME = "config";

  @Mock(lenient = true)
  protected ExtensionModel extensionModel;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  protected ConfigurationModel configurationModel;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  protected DefaultExecutionContext operationContext;

  @Mock(lenient = true)
  protected CoreEvent event;

  protected TestTimeSupplier timeSupplier = new TestTimeSupplier(System.currentTimeMillis());
  protected LifecycleAwareConfigurationProvider provider;

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(OBJECT_TIME_SUPPLIER, timeSupplier);
  }

  @Before
  public void before() throws Exception {
    muleContext.getInjector().inject(provider);
    spyInjector(muleContext);
  }

  @Test
  public void getName() {
    assertThat(provider.getName(), is(CONFIG_NAME));
  }

  @Test
  public void getConfigurationModel() {
    assertThat(provider.getConfigurationModel(), is(CoreMatchers.sameInstance(configurationModel)));
  }

  protected void assertSameInstancesResolved() throws Exception {
    final int count = 10;
    Object config = provider.get(event);

    for (int i = 1; i < count; i++) {
      assertThat(provider.get(event), is(sameInstance(config)));
    }
  }
}
