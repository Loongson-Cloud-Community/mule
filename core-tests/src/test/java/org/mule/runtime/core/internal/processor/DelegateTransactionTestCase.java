/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.processor;

import static org.mule.tck.util.MuleContextUtils.mockMuleContext;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.mule.runtime.core.api.SingleResourceTransactionFactoryManager;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.runtime.api.notification.NotificationDispatcher;
import org.mule.runtime.core.privileged.registry.RegistrationException;
import org.mule.runtime.core.internal.transaction.DelegateTransaction;
import org.mule.tck.util.MuleContextUtils;

import javax.transaction.TransactionManager;

import org.junit.Test;
import org.junit.Before;

public class DelegateTransactionTestCase extends AbstractMuleTestCase {

  private static final int DEFAULT_TX_TIMEOUT = 30000;

  private String applicationName = "appName";
  private NotificationDispatcher notificationDispatcher;
  private TransactionManager transactionManager;
  private SingleResourceTransactionFactoryManager transactionFactoryManager;

  @Before
  public void detUp() throws RegistrationException {
    notificationDispatcher = MuleContextUtils.getNotificationDispatcher(mockMuleContext());
    transactionManager = mock(TransactionManager.class);
    transactionFactoryManager = new SingleResourceTransactionFactoryManager();
  }

  @Test
  public void defaultTxTimeout() {
    DelegateTransaction delegateTransaction = new DelegateTransaction(applicationName, notificationDispatcher,
                                                                      transactionFactoryManager,
                                                                      transactionManager);
    assertThat(delegateTransaction.getTimeout(), is(DEFAULT_TX_TIMEOUT));
  }

  @Test
  public void changeTxTimeout() {
    DelegateTransaction delegateTransaction = new DelegateTransaction(applicationName, notificationDispatcher,
                                                                      transactionFactoryManager,
                                                                      transactionManager);
    int newTimeout = 10;
    delegateTransaction.setTimeout(newTimeout);
    assertThat(delegateTransaction.getTimeout(), is(newTimeout));
  }
}
