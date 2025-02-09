/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.transaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mule.runtime.extension.api.tx.OperationTransactionalAction.JOIN_IF_POSSIBLE;
import static org.mule.runtime.extension.api.tx.OperationTransactionalAction.NOT_SUPPORTED;
import static org.mule.runtime.extension.api.tx.SourceTransactionalAction.ALWAYS_BEGIN;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.extension.api.tx.OperationTransactionalAction;
import org.mule.runtime.extension.api.tx.SourceTransactionalAction;
import org.mule.test.module.extension.AbstractExtensionFunctionalTestCase;
import org.mule.test.transactional.TransactionalSourceWithTXParameters;

import org.junit.Test;

public class TransactionalActionParameterTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "transactional-parameters-injection-config.xml";
  }

  @Test
  public void injectAlwaysBeginSourceTransactionalAction() throws Exception {
    Reference<SourceTransactionalAction> sourceTransactionalAction = new Reference<>();
    TransactionalSourceWithTXParameters.responseCallback = tx -> sourceTransactionalAction.set((SourceTransactionalAction) tx);
    startFlow("alwaysBeginTxAction");

    assertThat(sourceTransactionalAction.get(), is(ALWAYS_BEGIN));
  }

  @Test
  public void injectDefaultOperationTransactionalAction() throws Exception {
    OperationTransactionalAction value =
        (OperationTransactionalAction) flowRunner("injectInOperationDefaultValue").run().getMessage().getPayload().getValue();
    assertThat(value, is(JOIN_IF_POSSIBLE));
  }

  @Test
  public void injectInOperationJoinNotSupported() throws Exception {
    OperationTransactionalAction value =
        (OperationTransactionalAction) flowRunner("injectInOperationJoinNotSupported").run().getMessage().getPayload().getValue();
    assertThat(value, is(NOT_SUPPORTED));
  }

  @Test
  public void sdkInjectDefaultOperationTransactionalAction() throws Exception {
    org.mule.sdk.api.tx.OperationTransactionalAction value =
        (org.mule.sdk.api.tx.OperationTransactionalAction) flowRunner("sdkInjectInOperationDefaultValue").run().getMessage()
            .getPayload().getValue();
    assertThat(value, is(org.mule.sdk.api.tx.OperationTransactionalAction.JOIN_IF_POSSIBLE));
  }

  @Test
  public void sdkInjectInOperationJoinNotSupported() throws Exception {
    org.mule.sdk.api.tx.OperationTransactionalAction value =
        (org.mule.sdk.api.tx.OperationTransactionalAction) flowRunner("sdkInjectInOperationJoinNotSupported").run().getMessage()
            .getPayload().getValue();
    assertThat(value, is(org.mule.sdk.api.tx.OperationTransactionalAction.NOT_SUPPORTED));
  }

  private void startFlow(String flowName) throws Exception {
    ((Flow) getFlowConstruct(flowName)).start();
  }
}
