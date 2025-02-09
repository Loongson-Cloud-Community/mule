/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.connection;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.core.api.Closeable;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.api.exception.MuleException;

/**
 * Strategy to implement different connection management mechanisms.
 * <p>
 * For example, whether connections should be pooled, tied to an OAuth token, cached, etc.
 *
 * @param <C> the generic type of the connection being managed by {@code this} instance
 *
 * @since 1.0
 */
abstract class ConnectionManagementStrategy<C> implements Closeable {

  protected final ConnectionProvider<C> connectionProvider;
  protected final MuleContext muleContext;

  /**
   * Creates a new instance
   *
   * @param connectionProvider the {@link ConnectionProvider} which will be used to manage the connections
   * @param muleContext        the application's {@link MuleContext}
   */
  ConnectionManagementStrategy(ConnectionProvider<C> connectionProvider, MuleContext muleContext) {
    this.connectionProvider = connectionProvider;
    this.muleContext = muleContext;
  }

  /**
   * Wraps a connection into a {@link ConnectionHandler} and returns it. This method is to be assumed thread-safe, but no
   * assumptions should be made on whether each invokation returns the same {@link ConnectionHandler} or if that return value is
   * wrapping the same underlying {@code Connection} instance.
   *
   * @return a {@link ConnectionHandler}
   * @throws ConnectionException if an exception was found trying to obtain the connection
   */
  abstract ConnectionHandler<C> getConnectionHandler() throws ConnectionException;

  /**
   * Closes all connections and resources allocated through {@code this} instance.
   *
   * @throws MuleException if an exception was found closing the connections
   */
  @Override
  public abstract void close() throws MuleException;
}
