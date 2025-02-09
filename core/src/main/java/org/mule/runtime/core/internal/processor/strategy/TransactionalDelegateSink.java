/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.processor.strategy;

import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static org.mule.runtime.core.api.transaction.TransactionCoordination.isTransactionActive;

import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.core.api.construct.BackPressureReason;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate {@link Sink} that uses one of two {@link Sink}'s depending on if a transaction is in context or not.
 */
final class TransactionalDelegateSink implements Sink, Disposable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalDelegateSink.class);

  private final Sink transactionalSink;
  private final Sink sink;

  public TransactionalDelegateSink(Sink transactionalSink, Sink sink) {
    this.transactionalSink = transactionalSink;
    this.sink = sink;
  }

  @Override
  public void accept(CoreEvent event) {
    if (isTransactionActive()) {
      transactionalSink.accept(event);
    } else {
      sink.accept(event);
    }
  }

  @Override
  public BackPressureReason emit(CoreEvent event) {
    if (isTransactionActive()) {
      return transactionalSink.emit(event);
    } else {
      return sink.emit(event);
    }
  }

  @Override
  public void dispose() {
    disposeIfNeeded(transactionalSink, LOGGER);
    disposeIfNeeded(sink, LOGGER);
  }
}
