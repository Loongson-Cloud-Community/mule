/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.functional;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.qameta.allure.Issue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.util.IOUtils;

import org.junit.Test;

public class ModuleWithPropertiesTestCase extends AbstractCeXmlExtensionMuleArtifactFunctionalTestCase {

  @Override
  protected String getModulePath() {
    return "modules/module-properties.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-using-module-properties.xml";
  }

  @Test
  public void testSetPayloadHardcodedFromModuleFlow() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadHardcodedFromModuleFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("hardcoded value from module"));
  }

  @Test
  public void testSetPayloadParamFromModuleFlow() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadParamFromModuleFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("new payload from module"));
  }

  @Test
  public void testSetPayloadConfigParamFlow() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadConfigParamFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("some config-value-parameter"));
  }

  @Test
  public void testSetPayloadConfigDefaultParamFlow() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadConfigDefaultParamFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("some default-config-value-parameter"));
  }

  @Test
  public void testSetPayloadConfigDefaultPropertyUseOptionalFlow() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadConfigDefaultPropertyUseOptionalFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("some default-config-value-parameter"));
  }

  @Test
  public void testSetPayloadAddParamAndPropertyValues() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadAddParamAndPropertyValues").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("a parameter value some config-value-parameter"));
  }

  @Test
  public void testSetPayloadConfigOptionalProperty() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadConfigOptionalProperty").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is(nullValue()));
  }

  @Test
  public void testSetPayloadHardcodedGlobalProperty() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadHardcodedGlobalProperty").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("a-constant-global-value"));
  }

  @Test
  public void testSetPayloadHardcodedSystemProperty() throws Exception {
    final String expected_value = System.getProperty("user.home");
    CoreEvent muleEvent = flowRunner("testSetPayloadHardcodedSystemProperty").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is(expected_value));
  }

  @Test
  public void testSetPayloadHardcodedFileProperty() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadHardcodedFileProperty").run();
    final String expectedContent =
        IOUtils.toString(currentThread().getContextClassLoader().getResourceAsStream("modules/module-properties-file.txt"))
            .trim();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is(
                                                                  expectedContent));
  }

  @Test
  @Issue("MULE-19035")
  public void testSetPayloadAddParamAndPropertyValueThruInternalOperationFlow() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadAddParamAndPropertyValueThruInternalOperationFlow").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(), is("a parameter value some config-value-parameter"));
  }

  @Test
  @Issue("MULE-19962")
  public void testSetPayloadPropertiesCallingNestedOperations() throws Exception {
    CoreEvent muleEvent = flowRunner("testSetPayloadPropertiesCallingNestedOperations").run();
    assertThat(muleEvent.getMessage().getPayload().getValue(),
               is("What are these? These are two nice properties, some default-config-value-parameter and some default-config-value-parameter"));
  }
}
