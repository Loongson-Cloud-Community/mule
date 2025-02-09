/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.queue;

import java.io.Serializable;

/**
 * Once a queue operations it's executed this operation creates the correct {@link QueueTransactionContext} based on the queue
 * type, persistent or transient.
 *
 * @param <T> type of the transactional context
 */
public class QueueTypeTransactionContextAdapter<T extends QueueTransactionContext> implements QueueTransactionContext {

  private final QueueTransactionContextFactory<T> queueTransactionContextFactory;
  private T transactionContext;

  public QueueTypeTransactionContextAdapter(QueueTransactionContextFactory<T> queueTransactionContextFactory) {
    this.queueTransactionContextFactory = queueTransactionContextFactory;
  }

  public boolean offer(QueueStore queue, Serializable item, long offerTimeout) throws InterruptedException {
    defineDelegate(queue);
    return transactionContext.offer(queue, item, offerTimeout);
  }

  public void untake(QueueStore queue, Serializable item) throws InterruptedException {
    defineDelegate(queue);
    transactionContext.untake(queue, item);
  }

  public void clear(QueueStore queue) throws InterruptedException {
    defineDelegate(queue);
    transactionContext.clear(queue);
  }

  public Serializable poll(QueueStore queue, long pollTimeout) throws InterruptedException {
    defineDelegate(queue);
    return transactionContext.poll(queue, pollTimeout);
  }

  public Serializable peek(QueueStore queue) throws InterruptedException {
    defineDelegate(queue);
    return transactionContext.peek(queue);
  }

  public int size(QueueStore queue) {
    defineDelegate(queue);
    return transactionContext.size(queue);
  }

  public T getTransactionContext() {
    defineDelegate();
    return transactionContext;
  }

  private void defineDelegate() {
    // if no transaction context defined then no operation was executed so we are in recovery mode.
    if (transactionContext == null) {
      transactionContext = queueTransactionContextFactory.createPersistentTransactionContext();
    }
  }

  private void defineDelegate(QueueStore queue) {
    // We don't know the type of Connector queue config until a queue is used. We need to fix this in Mule 4.0. See MULE-7420
    if (transactionContext == null) {
      if (queue.isPersistent()) {
        // Only makes sense to create a use a transaction journal for RecoverableQueueStore
        transactionContext = queueTransactionContextFactory.createPersistentTransactionContext();
      } else {
        transactionContext = queueTransactionContextFactory.createTransientTransactionContext();
      }
    }
  }

}
