/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.validation;

import static org.mule.test.allure.AllureConstants.MuleDsl.DslValidationStory.DSL_VALIDATION_STORY;
import static org.mule.test.allure.AllureConstants.MuleDsl.MULE_DSL;
import static org.mule.test.allure.AllureConstants.ReuseFeature.REUSE;
import static org.mule.test.allure.AllureConstants.ReuseFeature.ReuseStory.OPERATIONS;

import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.mule.runtime.ast.api.validation.Validation;
import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.util.Optional;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Features;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Test;

@Features({@Feature(MULE_DSL), @Feature(REUSE)})
@Stories({@Story(DSL_VALIDATION_STORY), @Story(OPERATIONS)})
public class OperationDoesNotHaveApikitRouterTestCase extends AbstractCoreValidationTestCase {

  private static final String XML_NAMESPACE_DEF = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
      "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "      xmlns:operation=\"http://www.mulesoft.org/schema/mule/operation\"" +
      "      xmlns:apikit=\"http://www.mulesoft.org/schema/mule/mule-apikit\"" +
      "      xsi:schemaLocation=\"\n" +
      "       http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\n" +
      "       http://www.mulesoft.org/schema/mule/operation http://www.mulesoft.org/schema/mule/operation/current/mule-operation.xsd\n"
      +
      "       http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd\">\n";
  private static final String XML_CLOSE = "</mule>";

  @Override
  protected Validation getValidation() {
    return new OperationDoesNotHaveApikitRouter();
  }

  @Test
  @Description("Checks that no validation message is returned if there is no operation")
  public void withoutOperation() {
    final Optional<ValidationResultItem> msg = runValidation(XML_NAMESPACE_DEF + XML_CLOSE).stream().findFirst();
    assertThat(msg, is(empty()));
  }

  @Test
  @Description("Checks that no validation message is returned if there is no apikit:router inside operation")
  public void operationWithoutApikitRouter() {
    final Optional<ValidationResultItem> msg = runValidation(XML_NAMESPACE_DEF +
        "<operation:def name=\"someOp\">" +
        "    <operation:body>" +
        "        <logger />" +
        "    </operation:body>" +
        "</operation:def>" +
        XML_CLOSE).stream().findFirst();
    assertThat(msg, is(empty()));
  }

  @Test
  @Description("Checks that no validation message is returned if there is an apikit:router inside a flow (backwards)")
  public void flowWithApikitRouter() {
    final Optional<ValidationResultItem> msg = runValidation(XML_NAMESPACE_DEF +
        "<flow name=\"someFlow\">" +
        "    <apikit:router config-ref=\"router-config\" />" +
        "</flow>" +
        XML_CLOSE).stream().findFirst();
    assertThat(msg, is(empty()));
  }

  @Test
  @Description("Checks that a corresponding validation message is returned if there is an apikit:router inside an operation")
  public void operationWithApikitRouter() {
    final Optional<ValidationResultItem> msg = runValidation(XML_NAMESPACE_DEF +
        "<operation:def name=\"someOp\">" +
        "    <operation:body>" +
        "        <apikit:router config-ref=\"router-config\" />" +
        "    </operation:body>" +
        "</operation:def>" +
        XML_CLOSE).stream().findFirst();
    assertThat(msg, is(not(empty())));
    assertThat(msg.get().getMessage(),
               containsString("Usages of the component 'apikit:router' are not allowed inside a Mule SDK Operation Definition"));
  }
}
