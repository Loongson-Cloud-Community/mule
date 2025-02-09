/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.execution;

import org.mule.runtime.api.notification.NotificationDispatcher;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.core.api.SingleResourceTransactionFactoryManager;
import org.mule.runtime.core.api.execution.ExecutionCallback;
import org.mule.runtime.core.api.transaction.Transaction;
import org.mule.runtime.core.api.transaction.TransactionConfig;
import org.mule.runtime.core.api.transaction.TransactionCoordination;
import org.mule.runtime.core.internal.exception.MessagingException;

import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeginAndResolveTransactionInterceptor<T> implements ExecutionInterceptor<T> {

  private static final Logger logger = LoggerFactory.getLogger(BeginAndResolveTransactionInterceptor.class);
  private final ExecutionInterceptor<T> next;
  private final TransactionConfig transactionConfig;
  private final String applicationName;
  private final SingleResourceTransactionFactoryManager transactionFactoryManager;
  private final TransactionManager transactionManager;
  private final NotificationDispatcher notificationDispatcher;
  private final boolean processOnException;
  private final boolean mustResolveAnyTransaction;

  public BeginAndResolveTransactionInterceptor(ExecutionInterceptor<T> next, TransactionConfig transactionConfig,
                                               String applicationName, NotificationDispatcher notificationDispatcher,
                                               SingleResourceTransactionFactoryManager transactionFactoryManager,
                                               TransactionManager transactionManager,
                                               boolean processOnException, boolean mustResolveAnyTransaction) {
    this.next = next;
    this.transactionConfig = transactionConfig;
    this.applicationName = applicationName;
    this.notificationDispatcher = notificationDispatcher;
    this.transactionFactoryManager = transactionFactoryManager;
    this.transactionManager = transactionManager;
    this.processOnException = processOnException;
    this.mustResolveAnyTransaction = mustResolveAnyTransaction;
  }

  @Override
  public T execute(ExecutionCallback<T> callback, ExecutionContext executionContext) throws Exception {
    byte action = transactionConfig.getAction();
    int timeout = transactionConfig.getTimeout();

    boolean resolveStartedTransaction = false;
    Transaction tx = TransactionCoordination.getInstance().getTransaction();
    if (action == TransactionConfig.ACTION_ALWAYS_BEGIN || (action == TransactionConfig.ACTION_BEGIN_OR_JOIN && tx == null)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Beginning transaction");
      }
      executionContext.markTransactionStart();

      // Timeout is a traversal attribute of all Transaction implementations.
      // Setting it up here for all of them rather than in every implementation.
      tx = transactionConfig.getFactory().beginTransaction(applicationName, notificationDispatcher, transactionFactoryManager,
                                                           transactionManager);
      tx.setTimeout(timeout);
      resolveStartedTransaction = true;
      if (logger.isDebugEnabled()) {
        logger.debug("Transaction successfully started: " + tx);
      }
    }
    T result;
    try {

      result = next.execute(callback, executionContext);
      resolveTransactionIfRequired(resolveStartedTransaction);
      return result;
    } catch (MessagingException e) {
      if (processOnException) {
        rollbackTransactionIfRequired(resolveStartedTransaction || mustResolveAnyTransaction);
      }
      throw e;
    }
  }

  private void rollbackTransactionIfRequired(boolean mustResolveTransaction) {
    Transaction transaction = TransactionCoordination.getInstance().getTransaction();
    if (mustResolveTransaction && transaction != null) {
      TransactionCoordination.getInstance().rollbackCurrentTransaction();
    }
  }

  private void resolveTransactionIfRequired(boolean mustResolveTransaction) throws TransactionException {
    Transaction transaction = TransactionCoordination.getInstance().getTransaction();
    if (mustResolveTransaction && transaction != null) {
      TransactionCoordination.getInstance().resolveTransaction();
    }
  }

}
