/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.transaction;

import static org.mule.runtime.api.tx.TransactionType.LOCAL;
import org.mule.runtime.api.notification.NotificationDispatcher;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.api.tx.TransactionType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.SingleResourceTransactionFactoryManager;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.cannotStartTransaction;

import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.runtime.core.internal.transaction.DelegateTransaction;
import org.mule.runtime.core.privileged.registry.RegistrationException;

import javax.transaction.TransactionManager;

/**
 * Transaction factory for DelegateTransaction. Used for transactional element since transaction type is not known until the first
 * transactional message processor is executed.
 */
public final class DelegateTransactionFactory implements TypedTransactionFactory {

  @Override
  public Transaction beginTransaction(MuleContext muleContext) throws TransactionException {
    try {
      return this.beginTransaction(muleContext.getConfiguration().getId(),
                                   ((MuleContextWithRegistry) muleContext).getRegistry()
                                       .lookupObject(NotificationDispatcher.class),
                                   muleContext.getTransactionFactoryManager(), muleContext.getTransactionManager());
    } catch (RegistrationException e) {
      throw new TransactionException(cannotStartTransaction("Delegate"), e);
    }
  }

  @Override
  public Transaction beginTransaction(String applicationName, NotificationDispatcher notificationFirer,
                                      SingleResourceTransactionFactoryManager transactionFactoryManager,
                                      TransactionManager transactionManager)
      throws TransactionException {
    DelegateTransaction delegateTransaction =
        new DelegateTransaction(applicationName, notificationFirer, transactionFactoryManager, transactionManager);
    delegateTransaction.begin();
    return delegateTransaction;
  }

  @Override
  public boolean isTransacted() {
    return true;
  }

  @Override
  public TransactionType getType() {
    return LOCAL;
  }

}
