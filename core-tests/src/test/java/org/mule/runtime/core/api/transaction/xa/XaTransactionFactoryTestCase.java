/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.transaction.xa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tck.util.MuleContextUtils.getNotificationDispatcher;
import static org.mule.tck.util.MuleContextUtils.mockContextWithServices;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.SingleResourceTransactionFactoryManager;
import org.mule.runtime.core.api.transaction.Transaction;
import org.mule.runtime.core.privileged.transaction.xa.XaTransactionFactory;
import org.mule.tck.junit4.AbstractMuleTestCase;

import javax.transaction.TransactionManager;

import org.junit.Test;

public class XaTransactionFactoryTestCase extends AbstractMuleTestCase {

  @Test
  public void setsTransactionTimeout() throws Exception {
    final int timeout = 1000;
    final XaTransactionFactory transactionFactory = new XaTransactionFactory();
    transactionFactory.setTimeout(timeout);

    final MuleContext muleContext = mockContextWithServices();

    final TransactionManager transactionManager = mock(TransactionManager.class);
    when(muleContext.getTransactionManager()).thenReturn(transactionManager);

    final Transaction transaction = transactionFactory.beginTransaction("appName", getNotificationDispatcher(muleContext),
                                                                        new SingleResourceTransactionFactoryManager(),
                                                                        muleContext.getTransactionManager());
    assertThat(transaction.getTimeout(), equalTo(timeout));
  }
}
