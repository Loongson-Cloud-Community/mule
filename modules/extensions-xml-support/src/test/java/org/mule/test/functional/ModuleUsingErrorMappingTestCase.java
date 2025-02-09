/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.functional;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.functional.junit4.matchers.ThatMatcher.that;
import static org.mule.runtime.api.util.MuleSystemProperties.MULE_LAX_ERROR_TYPES;
import static org.mule.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static org.mule.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.ERROR_MAPPINGS;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(ERROR_HANDLING)
@Story(ERROR_MAPPINGS)
public class ModuleUsingErrorMappingTestCase extends AbstractCeXmlExtensionMuleArtifactFunctionalTestCase {

  private static final String CONNECT_ERROR_MESSAGE = "Could not connect.";
  private static final String UNMATCHED_ERROR_MESSAGE = "Error.";
  private static final String EXPRESSION_ERROR_MESSAGE = "Bad expression.";
  private static final String TIMEOUT_ERROR_MESSAGE = "Timeout happened!";
  private static final String SECURITY_ERROR_MESSAGE = "simple operation called";

  @Rule
  public SystemProperty laxErrorType = new SystemProperty(MULE_LAX_ERROR_TYPES, "true");

  @Override
  protected String getModulePath() {
    return "modules/module-using-errormapping.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-with-module-using-errormapping.xml";
  }

  @Test
  @Description("Verifies that an unmapped error is handled as ANY.")
  public void simpleRequest() throws Exception {
    verifySuccessExpression("noMapping", UNMATCHED_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that each error is correctly handled given an operation without mappings.")
  public void multipleMappingsDirectlyFromSmartConnector() throws Exception {
    verifyFailingExpression("multipleMappingsDirectlyFromSmartConnector", EXPRESSION_ERROR_MESSAGE);
    verifySuccessExpression("multipleMappingsDirectlyFromSmartConnector", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via wildcard is handled.")
  public void mappedRequest() throws Exception {
    verifySuccessExpression("simpleMapping", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via a custom matcher is handled. ")
  public void matchingMappedRequest() throws Exception {
    verifySuccessExpression("complexMapping", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that an unmapped error is handled as ANY.")
  public void noMatchingMappedRequest() throws Exception {
    verifyFailingExpression("complexMapping", UNMATCHED_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that each error is correctly handled given an operation with multiple mappings.")
  public void multipleMappingsRequest() throws Exception {
    verifyFailingExpression("multipleMappings", EXPRESSION_ERROR_MESSAGE);
    verifySuccessExpression("multipleMappings", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via wildcard is handled through the proxy smart connector.")
  public void mappedRequestProxy() throws Exception {
    verifySuccessExpression("simpleMappingProxy", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via a custom matcher is handled through the proxy smart connector.")
  public void matchingMappedRequestProxy() throws Exception {
    verifySuccessExpression("complexMappingProxy", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that an unmapped error is handled as ANY through the proxy smart connector.")
  public void noMatchingMappedRequestProxy() throws Exception {
    verifyFailingExpression("complexMappingProxy", UNMATCHED_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that each error is correctly handled given an operation with multiple mappings through the proxy smart connector.")
  public void multipleMappingsRequestProxy() throws Exception {
    verifyFailingExpression("multipleMappingsProxy", EXPRESSION_ERROR_MESSAGE);
    verifySuccessExpression("multipleMappingsProxy", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via global error handler works.")
  public void globalMappingWithDefaultErrorHandlerTimeout() throws Exception {
    verifySuccessExpression("globalMappingWithDefaultErrorHandlerTimeout", TIMEOUT_ERROR_MESSAGE);
  }


  @Test
  @Description("Verifies that a mapped error via global error handler works, where the on continue holds an operation from a smart connector.")
  public void globalMappingWithDefaultErrorHandlerSecurity() throws Exception {
    verifySuccessExpression("globalMappingWithDefaultErrorHandlerSecurity", SECURITY_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via global error handler works using subflows.")
  @Ignore("MULE-14351")
  public void globalMappingWithDefaultErrorHandlerTimeoutThruSubflow() throws Exception {
    verifySuccessExpression("globalMappingWithDefaultErrorHandlerTimeoutThruSubflow", TIMEOUT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via global error handler works using nested subflows.")
  @Ignore("MULE-14351")
  public void globalMappingWithDefaultErrorHandlerTimeoutThruSubSubflow() throws Exception {
    verifySuccessExpression("globalMappingWithDefaultErrorHandlerTimeoutThruSubSubflow", TIMEOUT_ERROR_MESSAGE);
  }

  private void verifySuccessExpression(String flowName, String expectedPayload) throws Exception {
    verify(flowName, expectedPayload, false);
  }

  private void verifyFailingExpression(String flowName, String expectedPayload) throws Exception {
    verify(flowName, expectedPayload, true);
  }

  private void verify(String flowName, String expectedPayload, boolean failExpression) throws Exception {
    CoreEvent coreEvent = flowRunner(flowName)
        .withVariable("names", emptyMap())
        .withVariable("failExpression", failExpression)
        .run();
    assertThat(coreEvent.getMessage(), hasPayload(that(is(expectedPayload))));
  }

}
