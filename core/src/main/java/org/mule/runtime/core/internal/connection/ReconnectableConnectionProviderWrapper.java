/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.connection;

import static java.util.Optional.ofNullable;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.core.api.retry.policy.RetryPolicyTemplate;
import org.mule.runtime.core.internal.retry.ReconnectionConfig;

import java.util.Optional;

/**
 * A {@link ConnectionProviderWrapper} which includes a {@link RetryPolicyTemplate} for reconnecting in case of problems
 * establishing the connection.
 * <p>
 * It also contains the ability to skip connection validation.
 *
 * @param <C> The generic type of the connections provided by the {@link #delegate}
 * @since 4.0
 */
public class ReconnectableConnectionProviderWrapper<C> extends AbstractConnectionProviderWrapper<C> {

  private final ReconnectionConfig reconnectionConfig;

  /**
   * Creates a new instance
   *
   * @param delegate           the {@link ConnectionProvider} to be wrapped
   * @param reconnectionConfig The {@link ReconnectionConfig} for retrying failed connection attempts
   */
  public ReconnectableConnectionProviderWrapper(ConnectionProvider<C> delegate, ReconnectionConfig reconnectionConfig) {
    super(delegate);
    this.reconnectionConfig = reconnectionConfig;
  }

  /**
   * Delegates the responsibility of validating the connection to the delegated {@link ConnectionProvider}.
   *
   * @param connection a given connection
   * @return A {@link ConnectionValidationResult} returned by the delegated {@link ConnectionProvider}
   */
  @Override
  public ConnectionValidationResult validate(C connection) {
    return getDelegate().validate(connection);
  }

  @Override
  public Optional<ReconnectionConfig> getReconnectionConfig() {
    return ofNullable(reconnectionConfig);
  }
}
