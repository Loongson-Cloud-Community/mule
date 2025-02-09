/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.component.location.Location.builderFromStringRepresentation;
import static org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy.DROP;
import static org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy.FAIL;
import static org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy.WAIT;
import static org.mule.test.allure.AllureConstants.ExecutionEngineFeature.ExecutionEngineStory.BACKPRESSURE;
import static org.mule.test.allure.AllureConstants.SourcesFeature.SOURCES;

import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.core.api.source.MessageSource;
import org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy;
import org.mule.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SOURCES)
@Story(BACKPRESSURE)
public class BackPressureConfigTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "source/heisenberg-backpressure-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    componentLocator = registry.lookupByType(ConfigurationComponentLocator.class).get();
  }

  @Test
  public void defaultsToFail() {
    assertStrategy("defaultToFail", FAIL);
  }

  @Test
  public void configuredToDrop() {
    assertStrategy("configuredToDrop", DROP);
  }

  @Test
  public void inheritDefaultFromParentSourceClass() {
    assertStrategy("defaultCase", FAIL);
  }

  private void assertStrategy(String flowName, BackPressureStrategy expected) {
    MessageSource source =
        (MessageSource) componentLocator.find(builderFromStringRepresentation(flowName + "/source").build()).get();
    assertThat(source.getBackPressureStrategy(), is(expected));
  }
}
