/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.functional.transformer.simple;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.asList;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.tck.util.MuleContextUtils.eventBuilder;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.processor.simple.AbstractRemoveVariablePropertyProcessor;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.size.SmallTest;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SmallTest
public abstract class AbstractRemoveVariablePropertyProcessorTestCase extends AbstractMuleContextTestCase {

  public static final Charset ENCODING = US_ASCII;
  public static final String PLAIN_STRING_KEY = "someText";
  public static final String EXPRESSION = "#['someText']";
  public static final String EXPRESSION_VALUE = "expressionValueResult";
  public static final String NULL_EXPRESSION = "#[null]";

  private Message message;
  private CoreEvent event;
  private final AbstractRemoveVariablePropertyProcessor removeVariableProcessor;

  public AbstractRemoveVariablePropertyProcessorTestCase(AbstractRemoveVariablePropertyProcessor abstractAddVariableProcessor) {
    removeVariableProcessor = abstractAddVariableProcessor;
  }

  @Before
  public void setUpTest() throws Exception {
    message = of("");
    event = createTestEvent(message);

    removeVariableProcessor.setExpressionManager(muleContext.getExpressionManager());
  }

  protected CoreEvent createTestEvent(final Message message) throws MuleException {
    return eventBuilder(muleContext)
        .addVariable(PLAIN_STRING_KEY, EXPRESSION_VALUE)
        .message(message)
        .build();
  }

  @Test
  public void testRemoveVariable() throws MuleException {
    removeVariableProcessor.setIdentifier(PLAIN_STRING_KEY);
    removeVariableProcessor.initialise();
    event = removeVariableProcessor.process(event);
    verifyRemoved(event, PLAIN_STRING_KEY);
  }

  @Test
  public void testRemoveVariableUsingExpression() throws MuleException {
    removeVariableProcessor.setIdentifier(EXPRESSION);
    removeVariableProcessor.initialise();
    event = removeVariableProcessor.process(event);
    verifyRemoved(event, EXPRESSION_VALUE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveVariableNullKey() {
    removeVariableProcessor.setIdentifier(null);
  }

  @Test // Don't fail.
  public void testRemoveVariableExpressionKeyNullValue() throws MuleException {
    removeVariableProcessor.setIdentifier(NULL_EXPRESSION);
    removeVariableProcessor.initialise();
    event = removeVariableProcessor.process(event);
  }

  protected abstract void addMockedPropeerties(CoreEvent event, Set<String> properties);

  protected abstract void verifyRemoved(CoreEvent mockEvent, String key);

  protected abstract void verifyNotRemoved(CoreEvent mockEvent, String somevar);

}
