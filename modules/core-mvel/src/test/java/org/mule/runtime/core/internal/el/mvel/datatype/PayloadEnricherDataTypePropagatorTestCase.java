/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.datatype;

import static java.nio.charset.StandardCharsets.UTF_16;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.mvel2.MVEL.compileExpression;
import static org.mule.runtime.api.metadata.MediaType.JSON;
import static org.mule.runtime.core.internal.el.mvel.MessageVariableResolverFactory.MESSAGE_PAYLOAD;
import static org.mule.runtime.core.internal.el.mvel.MessageVariableResolverFactory.PAYLOAD;
import static org.mule.tck.junit4.matcher.DataTypeMatcher.like;

import org.mule.mvel2.ParserContext;
import org.mule.mvel2.compiler.CompiledExpression;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.el.mvel.MVELExpressionLanguage;
import org.mule.runtime.core.privileged.event.PrivilegedEvent;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.nio.charset.Charset;

import org.junit.Test;

public class PayloadEnricherDataTypePropagatorTestCase extends AbstractMuleContextTestCase {

  public static final Charset CUSTOM_ENCODING = UTF_16;

  private final PayloadEnricherDataTypePropagator dataTypePropagator = new PayloadEnricherDataTypePropagator();

  @Override
  protected boolean mockExprExecutorService() {
    return true;
  }

  @Test
  public void propagatesPayloadDataType() throws Exception {
    doPayloadDataTypeTest(PAYLOAD + " = 'unused'");
  }

  @Test
  public void propagatesMessagePayloadDataType() throws Exception {
    doPayloadDataTypeTest(MESSAGE_PAYLOAD + " = 'unused'");
  }

  private void doPayloadDataTypeTest(String expression) throws Exception {
    final DataType expectedDataType = DataType.builder().type(String.class).mediaType(JSON).charset(CUSTOM_ENCODING).build();

    MVELExpressionLanguage expressionLanguage = new MVELExpressionLanguage(muleContext);
    final CompiledExpression compiledExpression =
        (CompiledExpression) compileExpression(expression, new ParserContext(expressionLanguage.getParserConfiguration()));

    final PrivilegedEvent.Builder builder = PrivilegedEvent.builder(testEvent());
    dataTypePropagator.propagate((PrivilegedEvent) testEvent(), builder, new TypedValue<>(TEST_MESSAGE, expectedDataType),
                                 compiledExpression);
    final CoreEvent event = builder.build();

    assertThat(event.getMessage().getPayload().getDataType(), like(String.class, JSON, CUSTOM_ENCODING));
  }
}
