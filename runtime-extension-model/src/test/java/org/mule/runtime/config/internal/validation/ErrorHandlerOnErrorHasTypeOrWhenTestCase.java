/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.validation;

import static org.mule.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static org.mule.test.allure.AllureConstants.MuleDsl.MULE_DSL;
import static org.mule.test.allure.AllureConstants.MuleDsl.DslValidationStory.DSL_VALIDATION_STORY;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.mule.runtime.ast.api.validation.Validation;
import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.util.Optional;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Features;
import io.qameta.allure.Story;

@Features({@Feature(ERROR_HANDLING), @Feature(MULE_DSL)})
@Story(DSL_VALIDATION_STORY)
public class ErrorHandlerOnErrorHasTypeOrWhenTestCase extends AbstractCoreValidationTestCase {

  @Override
  protected Validation getValidation() {
    return new ErrorHandlerOnErrorHasTypeOrWhen();
  }

  @Test
  public void errorHandlerCantHaveOnErrorWithoutTypeOrExpression() {
    final Optional<ValidationResultItem> msg = runValidation("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
        "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "      xsi:schemaLocation=\"\n" +
        "       http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n" +
        "\n" +
        "    <flow name=\"flowA\">\n" +
        "        <logger/>\n" +
        "        <error-handler>\n" +
        "            <on-error-continue/>\n" +
        "            <on-error-continue/>\n" +
        "        </error-handler>\n" +
        "    </flow>\n" +
        "\n" +
        "</mule>")
            .stream().findFirst();

    assertThat(msg.get().getMessage(),
               containsString("Every handler (except for the last one) within an 'error-handler' must specify a 'when' or 'type' attribute."));
  }

  @Test
  public void onErrorReferenceMustHaveTypeOrExpression() {
    final Optional<ValidationResultItem> msg = runValidation("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
        "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "      xsi:schemaLocation=\"\n" +
        "       http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n" +
        "\n" +
        "    <on-error-propagate name=\"sharedOnError\">\n" +
        "        <logger/>\n" +
        "    </on-error-propagate>\n" +
        "\n" +
        "    <flow name=\"flowA\">\n" +
        "        <logger/>\n" +
        "        <error-handler>\n" +
        "            <on-error ref=\"sharedOnError\"/>\n" +
        "            <on-error-continue/>\n" +
        "        </error-handler>\n" +
        "    </flow>\n" +
        "\n" +
        "</mule>")
            .stream().findFirst();

    assertThat(msg.get().getMessage(),
               containsString("Every handler (except for the last one) within an 'error-handler' must specify a 'when' or 'type' attribute."));
  }

}
