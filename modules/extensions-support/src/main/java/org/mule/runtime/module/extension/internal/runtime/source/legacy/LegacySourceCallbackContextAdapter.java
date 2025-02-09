/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.source.legacy;

import static java.util.Collections.emptyList;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.core.internal.execution.NotificationFunction;
import org.mule.runtime.extension.api.notification.NotificationActionDefinition;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.tx.TransactionHandle;
import org.mule.runtime.module.extension.internal.runtime.source.SourceCallbackContextAdapter;
import org.mule.runtime.module.extension.internal.runtime.transaction.legacy.LegacyTransactionHandle;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;

import java.util.List;
import java.util.Optional;

/**
 * Adapts a {@link SourceCallbackContext} into a legacy
 * {@link org.mule.runtime.extension.api.runtime.source.SourceCallbackContext}
 *
 * @since 4.4.0
 */
public class LegacySourceCallbackContextAdapter implements AugmentedLegacySourceCallbackContext {

  private final org.mule.sdk.api.runtime.source.SourceCallbackContext delegate;

  public LegacySourceCallbackContextAdapter(org.mule.sdk.api.runtime.source.SourceCallbackContext delegate) {
    this.delegate = delegate;
  }

  @Override
  public TransactionHandle bindConnection(Object connection) throws ConnectionException, TransactionException {
    return new LegacyTransactionHandle(delegate.bindConnection(connection));
  }

  @Override
  public <T> T getConnection() throws IllegalStateException {
    return delegate.getConnection();
  }

  @Override
  public TransactionHandle getTransactionHandle() {
    return new LegacyTransactionHandle(delegate.getTransactionHandle());
  }

  @Override
  public boolean hasVariable(String variableName) {
    return delegate.hasVariable(variableName);
  }

  @Override
  public <T> Optional<T> getVariable(String variableName) {
    return delegate.getVariable(variableName);
  }

  @Override
  public void addVariable(String variableName, Object value) {
    delegate.addVariable(variableName, value);
  }

  @Override
  public void setCorrelationId(String correlationId) {
    delegate.setCorrelationId(correlationId);
  }

  @Override
  public Optional<String> getCorrelationId() {
    return delegate.getCorrelationId();
  }

  @Override
  public <T, A> SourceCallback<T, A> getSourceCallback() {
    return new LegacySourceCallbackAdapter<>(delegate.getSourceCallback());
  }

  @Override
  public void fireOnHandle(NotificationActionDefinition<?> action, TypedValue<?> data) {
    delegate.fireOnHandle(action, data);
  }

  @Override
  public void releaseConnection() {
    if (delegate instanceof SourceCallbackContextAdapter) {
      ((SourceCallbackContextAdapter) delegate).releaseConnection();
    }
  }

  @Override
  public void dispatched() {
    if (delegate instanceof SourceCallbackContextAdapter) {
      ((SourceCallbackContextAdapter) delegate).dispatched();
    }
  }

  @Override
  public List<NotificationFunction> getNotificationsFunctions() {
    if (delegate instanceof SourceCallbackContextAdapter) {
      return ((SourceCallbackContextAdapter) delegate).getNotificationsFunctions();
    }
    return emptyList();
  }

  public DistributedTraceContextManager getDistributedSourceTraceContext() {
    return delegate.getDistributedSourceTraceContext();
  }
}
