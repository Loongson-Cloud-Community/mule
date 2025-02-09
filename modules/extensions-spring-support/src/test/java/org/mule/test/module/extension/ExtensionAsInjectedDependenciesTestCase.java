/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.message.Message.of;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.test.heisenberg.extension.HeisenbergExtension;
import org.mule.test.module.extension.internal.util.ExtensionsTestUtils;

import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

public class ExtensionAsInjectedDependenciesTestCase extends AbstractExtensionFunctionalTestCase {

  private static final String STATIC_HEISENBERG = "staticHeisenberg";
  private static final String DYNAMIC_AGE_HEISENBERG = "dynamicAgeHeisenberg";

  private Dependent dependent;

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    dependent = muleContext.getInjector().inject(new Dependent());
  }

  @Override
  protected String getConfigFile() {
    return "heisenberg-injected.xml";
  }

  @Test
  public void staticHeisenbergWasInjected() throws Exception {
    assertCorrectProviderInjected(STATIC_HEISENBERG, dependent.getStaticHeisenberg());
    HeisenbergExtension heisenberg =
        ExtensionsTestUtils.getConfigurationFromRegistry(STATIC_HEISENBERG, testEvent(), muleContext);
    assertThat(heisenberg.getPersonalInfo().getAge(), is(50));
  }

  @Test
  public void dynamicHeisenbergWasInjected() throws Exception {
    assertCorrectProviderInjected(DYNAMIC_AGE_HEISENBERG, dependent.getDynamicAgeHeisenberg());

    final int age = 52;
    CoreEvent event = CoreEvent.builder(testEvent()).message(of("")).addVariable("age", age).build();

    HeisenbergExtension heisenberg = ExtensionsTestUtils.getConfigurationFromRegistry(DYNAMIC_AGE_HEISENBERG, event, muleContext);
    assertThat(heisenberg.getPersonalInfo().getAge(), is(age));
  }

  private void assertCorrectProviderInjected(String key, ConfigurationProvider expected) {
    assertThat(expected, is(sameInstance(registry.lookupByName(key).get())));
  }

  public static class Dependent {

    @Inject
    @Named(STATIC_HEISENBERG)
    private ConfigurationProvider staticHeisenberg;

    @Inject
    @Named(DYNAMIC_AGE_HEISENBERG)
    private ConfigurationProvider dynamicAgeHeisenberg;

    public ConfigurationProvider getStaticHeisenberg() {
      return staticHeisenberg;
    }

    public ConfigurationProvider getDynamicAgeHeisenberg() {
      return dynamicAgeHeisenberg;
    }
  }
}
