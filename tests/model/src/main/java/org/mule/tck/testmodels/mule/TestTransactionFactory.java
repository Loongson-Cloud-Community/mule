/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.testmodels.mule;

import org.mule.runtime.api.notification.NotificationDispatcher;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.SingleResourceTransactionFactoryManager;
import org.mule.runtime.core.api.transaction.Transaction;
import org.mule.runtime.core.api.transaction.TransactionFactory;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;

import javax.transaction.TransactionManager;

/**
 * <code>TestTransactionFactory</code> creates a {@link org.mule.tck.testmodels.mule.TestTransaction}
 */

public class TestTransactionFactory implements TransactionFactory {

  // for testing properties
  private String value;
  private Transaction mockTransaction;

  public TestTransactionFactory() {}

  public TestTransactionFactory(Transaction mockTransaction) {
    this.mockTransaction = mockTransaction;
  }

  @Override
  public Transaction beginTransaction(MuleContext muleContext) {
    try {
      return beginTransaction(muleContext.getConfiguration().getId(),
                              ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(NotificationDispatcher.class),
                              muleContext.getTransactionFactoryManager(), muleContext.getTransactionManager());
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public Transaction beginTransaction(String applicationName, NotificationDispatcher notificationFirer,
                                      SingleResourceTransactionFactoryManager transactionFactoryManager,
                                      TransactionManager transactionManager)
      throws TransactionException {
    Transaction testTransaction;
    if (mockTransaction != null) {
      testTransaction = mockTransaction;
    } else {
      testTransaction = new TestTransaction(applicationName, notificationFirer, false);
    }

    testTransaction.begin();
    return testTransaction;
  }

  public boolean isTransacted() {
    return true;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
