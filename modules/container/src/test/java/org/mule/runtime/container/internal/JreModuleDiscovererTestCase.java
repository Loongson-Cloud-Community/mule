/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.container.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mule.runtime.container.internal.JreModuleDiscoverer.JRE_MODULE_NAME;

import org.mule.runtime.container.api.MuleModule;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Issue;

public class JreModuleDiscovererTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private final JreModuleDiscoverer moduleDiscoverer = new JreModuleDiscoverer();;

  @Test
  public void discoversJreModule() throws Exception {
    final List<MuleModule> muleModules = moduleDiscoverer.discover();

    assertThat(muleModules.size(), equalTo(1));
    final MuleModule muleModule = muleModules.get(0);
    assertThat(muleModule.getName(), equalTo(JRE_MODULE_NAME));
    assertThat(muleModule.getExportedPaths(), is(not(empty())));
    assertThat(muleModule.getExportedPackages(), is(not(empty())));
    assertThat(muleModule.getExportedServices(), is(not(empty())));
  }

  @Test
  @Issue("MULE-19398")
  public void discoveryResultCached() throws Exception {
    final List<MuleModule> discovered = moduleDiscoverer.discover();
    final List<MuleModule> discoveredFromCache = moduleDiscoverer.discover();

    assertThat(discoveredFromCache, sameInstance(discovered));
  }
}
