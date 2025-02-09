/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.event;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mule.runtime.core.internal.event.EventQuickCopy.quickCopy;

import io.qameta.allure.Issue;

import org.junit.Test;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.internal.message.InternalEvent;
import org.mule.tck.junit4.AbstractMuleTestCase;

public class DefaultEventBuilderTestCase extends AbstractMuleTestCase {

  private static final int NEW_EVENT_CREATIONS = 10000;
  private static final String INTERNAL_PARAMETER_KEY = "key";
  private static final String INTERNAL_PARAMETER_VALUE = "value";
  private static final String CORRELATION_ID = "correlationId";

  @Test
  @Issue("MULE-19180")
  public void stackOverflowOverInternalParameter() throws MuleException {
    InternalEvent quickCopy = quickCopy(newEvent(), singletonMap(INTERNAL_PARAMETER_KEY, INTERNAL_PARAMETER_VALUE));

    // Setting correlation to force a new event instance
    InternalEvent event = new DefaultEventBuilder(quickCopy).correlationId(CORRELATION_ID).build();
    for (int i = 0; i < NEW_EVENT_CREATIONS; i++) {
      // Setting correlation to force a new event instance
      event = new DefaultEventBuilder(event).correlationId(CORRELATION_ID).build();
    }
    assertThat(event.getInternalParameters().size(), is(1));
    assertThat(event.getInternalParameters().keySet(), hasItem(INTERNAL_PARAMETER_KEY));
  }
}
