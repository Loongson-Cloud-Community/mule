/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.extension.test.extension.reconnection;

import static org.mule.sdk.api.connectivity.ConnectionValidationResult.success;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.sdk.api.connectivity.CachedConnectionProvider;
import org.mule.sdk.api.connectivity.ConnectionValidationResult;


/**
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be declared in the connection
 * providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider will
 * be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and caches
 * connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
public class ReconnectableConnectionProvider implements CachedConnectionProvider<ReconnectableConnection> {

  public static volatile boolean fail;
  private int reconnectionAttempts = 0;
  public static volatile int disconnectCalls = 0;

  @Override
  public ReconnectableConnection connect() throws ConnectionException {
    if (fail) {
      reconnectionAttempts++;
      if (reconnectionAttempts <= 3) {
        throw new ConnectionException("FAAAAIL");
      }
      fail = false;
    }

    ReconnectableConnection connection = new ReconnectableConnection(reconnectionAttempts);
    reconnectionAttempts = 0;

    return connection;
  }

  @Override
  public void disconnect(ReconnectableConnection connection) {
    disconnectCalls++;
  }

  @Override
  public ConnectionValidationResult validate(ReconnectableConnection connection) {
    return success();
  }
}
