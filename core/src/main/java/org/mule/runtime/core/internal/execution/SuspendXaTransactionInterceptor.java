/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.execution;

import org.mule.runtime.core.api.execution.ExecutionCallback;
import org.mule.runtime.core.api.transaction.Transaction;
import org.mule.runtime.core.api.transaction.TransactionConfig;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.core.api.transaction.TransactionCoordination;
import org.mule.runtime.core.internal.exception.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuspendXaTransactionInterceptor<T> implements ExecutionInterceptor<T> {

  private static final Logger logger = LoggerFactory.getLogger(SuspendXaTransactionInterceptor.class);
  private final ExecutionInterceptor<T> next;
  private final TransactionConfig transactionConfig;
  private final boolean processOnException;

  public SuspendXaTransactionInterceptor(ExecutionInterceptor<T> next, TransactionConfig transactionConfig,
                                         boolean processOnException) {
    this.next = next;
    this.transactionConfig = transactionConfig;
    this.processOnException = processOnException;
  }

  @Override
  public T execute(ExecutionCallback<T> callback, ExecutionContext executionContext) throws Exception {
    Transaction suspendedXATx = null;
    Transaction tx = TransactionCoordination.getInstance().getTransaction();
    byte action = transactionConfig.getAction();
    if ((action == TransactionConfig.ACTION_NONE || action == TransactionConfig.ACTION_ALWAYS_BEGIN) && tx != null && tx.isXA()) {
      if (logger.isDebugEnabled()) {
        logger.debug("suspending XA tx " + action + ", " + "current TX: " + tx);
      }
      suspendedXATx = tx;
      suspendXATransaction(suspendedXATx);
    }
    try {
      T result = next.execute(callback, executionContext);
      resumeXaTransactionIfRequired(suspendedXATx);
      return result;
    } catch (MessagingException e) {
      if (processOnException) {
        TransactionCoordination.getInstance().resumeXaTransactionIfAvailable();
      }
      throw e;
    }
  }

  protected void suspendXATransaction(Transaction tx) throws TransactionException {
    TransactionCoordination.getInstance().suspendCurrentTransaction();
  }

  protected void resumeXATransaction(Transaction tx) throws TransactionException {
    TransactionCoordination.getInstance().resumeSuspendedTransaction();
  }

  private void resumeXaTransactionIfRequired(Transaction suspendedXATx) throws TransactionException {
    if (suspendedXATx != null) {
      resumeXATransaction(suspendedXATx);
    }
  }

}

