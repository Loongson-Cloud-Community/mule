/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.mule.runtime.core.api.util.ExceptionUtils.extractConnectionException;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.util.LazyValue;
import org.mule.runtime.core.api.connector.ConnectionManager;
import org.mule.runtime.core.api.util.func.CheckedFunction;
import org.mule.runtime.core.api.util.func.CheckedSupplier;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Resolving context, provides access to the Config and Connection
 *
 * @since 4.0
 */
public class ExtensionResolvingContext {

  private final LazyValue<Optional<ConfigurationInstance>> configurationInstance;
  private final LazyValue<Optional<ConnectionHandler>> connectionHandler;
  private final LazyValue<Optional<ConnectionProvider>> connectionProvider;


  /**
   * Retrieves the configuration for the related component
   *
   * @param configurationSupplier Supplies optional configurations
   * @param connectionManager     {@link ConnectionManager} which is able to find a connection for the component using the
   *                              {@param configInstance}
   */
  public ExtensionResolvingContext(Supplier<Optional<ConfigurationInstance>> configurationSupplier,
                                   ConnectionManager connectionManager) {
    this.configurationInstance = new LazyValue<>(configurationSupplier);
    this.connectionHandler = new LazyValue<>((CheckedSupplier<Optional<ConnectionHandler>>) () -> {
      Optional<ConfigurationInstance> configurationInstance = this.configurationInstance.get();
      if (configurationInstance.isPresent() && configurationInstance.get().getConnectionProvider().isPresent()) {
        return ofNullable(connectionManager.getConnection(configurationInstance.get().getValue()));
      }
      return empty();
    });
    this.connectionProvider = new LazyValue<>(() -> {
      Optional<ConfigurationInstance> configurationInstance = this.configurationInstance.get();
      if (configurationInstance.isPresent() && configurationInstance.get().getConnectionProvider().isPresent()) {
        return of(configurationInstance.get().getConnectionProvider().get());
      }
      return empty();
    });
  }

  /**
   * @param <C> Configuration type
   * @return optional configuration of a component
   */
  public <C> Optional<C> getConfig() {
    return (Optional<C>) configurationInstance.get().map(ConfigurationInstance::getValue);
  }

  /**
   * Retrieves the connection provider for the related a component and configuration
   *
   * @return Optional connection instance of {@link ConnectionProvider} type for the component.
   */
  public Optional<ConnectionProvider> getConnectionProvider() {
    return connectionProvider.get();
  }

  /**
   * Retrieves the connection for the related component and configuration
   *
   * @param <C> Connection type
   * @return A connection instance of {@param <C>} type for the component. If the related configuration does not require a
   *         connection {@link Optional#empty()} will be returned
   * @throws ConnectionException when no valid connection is found for the related component and configuration
   */
  public <C> Optional<C> getConnection() throws ConnectionException {
    try {
      return (Optional<C>) connectionHandler.get()
          .map((CheckedFunction<ConnectionHandler, Object>) ConnectionHandler::getConnection);
    } catch (Exception e) {
      Optional<ConnectionException> connectionException = extractConnectionException(e);
      throw connectionException.orElse(new ConnectionException(e));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void dispose() {
    connectionHandler
        .ifComputed(optionalHandler -> optionalHandler
            .ifPresent(ConnectionHandler::release));
  }
}
