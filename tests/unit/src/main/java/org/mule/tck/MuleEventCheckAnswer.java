/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MuleEventCheckAnswer extends Object implements Answer<CoreEvent> {

  private CoreEvent returnEvent;
  private MuleException exceptionToThrow;

  public MuleEventCheckAnswer() {
    this((CoreEvent) null);
  }

  public MuleEventCheckAnswer(CoreEvent returnEvent) {
    this.returnEvent = returnEvent;
  }

  public MuleEventCheckAnswer(MuleException ex) {
    this.exceptionToThrow = ex;
  }

  @Override
  public CoreEvent answer(InvocationOnMock invocation) throws Throwable {
    if (exceptionToThrow != null) {
      throw exceptionToThrow;
    } else {
      return checkInvocation(invocation);
    }
  }

  private CoreEvent checkInvocation(InvocationOnMock invocation) {
    Object[] arguments = invocation.getArguments();
    assertEquals(1, arguments.length);
    assertTrue(arguments[0] instanceof CoreEvent);

    return returnEvent;
  }
}
