/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.scopes;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.processor.chain.HasMessageProcessors;

import org.hamcrest.core.IsSame;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Issue;

public class ScopeExecutionTestCase extends AbstractScopeExecutionTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void fieldParameterInjection() throws Exception {
    Integer value = (Integer) flowRunner("scopeField")
        .withVariable("expected", 0)
        .withVariable("newValue", 1)
        .run().getMessage().getPayload().getValue();
    assertThat(value, is(1));

    value = (Integer) flowRunner("scopeField")
        .withVariable("expected", 1)
        .withVariable("newValue", 5)
        .run().getMessage().getPayload().getValue();
    assertThat(value, is(5));
  }

  @Test
  public void verifyProcessorInitialise() throws Exception {
    runFlow("getChain").getMessage().getPayload().getValue();
    runFlow("getChain").getMessage().getPayload().getValue();
    runFlow("getChain").getMessage().getPayload().getValue();
    int value = (int) runFlow("getCounter").getMessage().getPayload().getValue();
    assertThat(value, is(1));
  }

  @Test
  public void verifySameProcessorInstance() throws Exception {
    HasMessageProcessors getChainFirst = (HasMessageProcessors) runFlow("getChain").getMessage().getPayload().getValue();
    HasMessageProcessors getChainSecond = (HasMessageProcessors) runFlow("getChain").getMessage().getPayload().getValue();
    assertThat(getChainFirst, is(not(sameInstance(getChainSecond))));

    assertThat("Multiple executions of the same scope should yield the same processor instances",
               getChainFirst.getMessageProcessors(),
               contains(getChainSecond.getMessageProcessors().stream()
                   .map(IsSame::sameInstance)
                   .collect(toList())));
  }

  @Test
  public void alwaysFailsWrapperFailure() throws Exception {
    // Exceptions are converted in the extension's exception enricher
    expectedException.expectCause(instanceOf(ConnectionException.class));
    expectedException.expectMessage("ON_ERROR_ERROR");
    runFlow("alwaysFailsWrapperFailure");
  }

  @Test
  public void alwaysFailsWrapperSuccess() throws Exception {
    expectedException.expectCause(instanceOf(ConnectionException.class));
    expectedException.expectMessage("ON_SUCCESS_ERROR");
    runFlow("alwaysFailsWrapperSuccess");
  }

  @Test
  public void exceptionOnCallbacksSuccess() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    // When an exception occurs in the "onSuccess", we then invoke the onError
    expectedException.expectMessage("ON_ERROR_EXCEPTION");
    runFlow("exceptionOnCallbacksSuccess");
  }

  @Test
  public void exceptionOnCallbacksFailure() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("ON_ERROR_EXCEPTION");
    runFlow("exceptionOnCallbacksFailure");
  }

  @Test
  public void anything() throws Exception {
    CoreEvent event = flowRunner("executeAnything")
        .withPayload("Killed the following because I'm the one who knocks:").run();
    String expected = "Killed the following because I'm the one who knocks:";

    assertThat(event.getMessage().getPayload().getValue(), is(expected));
  }

  @Test
  public void neverFailsWrapperFailingChain() throws Exception {
    CoreEvent event = flowRunner("neverFailsWrapperFailingChain").run();

    assertThat(event.getMessage().getPayload().getValue(), is("ERROR"));
    assertThat(event.getVariables().get("varName").getValue(), is("varValue"));
  }

  @Test
  public void neverFailsWrapperSuccessChain() throws Exception {
    CoreEvent event = flowRunner("neverFailsWrapperSuccessChain")
        .withVariable("newpayload", "newpayload2")
        .run();

    assertThat(event.getMessage().getPayload().getValue(), is("SUCCESS"));
    assertThat(event.getVariables().get("varName").getValue(), is("varValue"));
  }

  @Test
  public void payloadModifier() throws Exception {
    CoreEvent event = flowRunner("payloadModifier").run();

    assertThat(event.getMessage().getPayload().getValue(), is("MESSAGE"));
    assertThat(event.getVariables().get("newPayload").getValue(), is("MESSAGE"));
    assertThat(event.getVariables().get("newAttributes").getValue(), is(notNullValue()));
  }

  @Test
  public void neverFailsWrapperNoChain() throws Exception {
    CoreEvent event = flowRunner("neverFailsWrapperNoChain").run();

    assertThat(event.getMessage().getPayload().getValue(), is("EMPTY"));
  }

  @Test
  public void scopeExecutionDoesntBlockThreads() throws Exception {
    testScheduler.submit(() -> flowRunner("executeNonBlocking").withPayload(TEST_MESSAGE).run());
    int threadsToCreate = (getRuntime().availableProcessors() * 2) - 1;
    for (int i = 0; i < threadsToCreate; ++i) {
      testScheduler.submit(() -> flowRunner("executeNonBlocking").withPayload(TEST_MESSAGE).run());
    }
    Thread.sleep(2000);
    cpuLightScheduler.submit(() -> {
    }).get(5, SECONDS);
  }

  @Test
  public void scopeUsingMuleAllowedStereotypes() throws Exception {
    String result = (String) flowRunner("scopeWithMuleAllowedStereotype").run().getMessage().getPayload().getValue();
    assertThat(result, is("Ok"));
  }

  @Test
  @Issue("MULE-18938")
  public void scopeChainLazilyStarted() throws Exception {
    String result = (String) flowRunner("scopeChainLazilyStarted").run().getMessage().getPayload().getValue();
    assertThat(result, is("newPayload"));
  }

}
