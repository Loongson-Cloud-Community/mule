/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.connection;

import static org.mule.runtime.api.meta.model.connection.ConnectionManagementType.CACHED;
import static org.mule.runtime.api.meta.model.connection.ConnectionManagementType.NONE;
import static org.mule.runtime.api.meta.model.connection.ConnectionManagementType.POOLING;
import static org.mule.runtime.core.api.connection.util.ConnectionProviderUtils.unwrapProviderWrapper;

import org.mule.runtime.api.config.PoolingProfile;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.PoolingListener;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.api.meta.model.connection.ConnectionManagementType;

/**
 * Creates instances of {@link ConnectionManagementStrategy}
 *
 * @since 4.0
 */
final class ConnectionManagementStrategyFactory {

  private final PoolingProfile defaultPoolingProfile;
  private final MuleContext muleContext;

  /**
   * Creates a new instance
   *
   * @param defaultPoolingProfile the {@link PoolingProfile} that will be used to configure the pool of connections
   * @param muleContext           the owning {@link MuleContext}
   */
  ConnectionManagementStrategyFactory(PoolingProfile defaultPoolingProfile, MuleContext muleContext) {
    this.defaultPoolingProfile = defaultPoolingProfile;
    this.muleContext = muleContext;
  }

  /**
   * Returns the management strategy that should be used for the given {@code connectionProvider}
   *
   * @param connectionProvider a {@link ConnectionProvider}
   * @param <C>                the generic type of the connections to be managed
   * @return a {@link ConnectionManagementStrategy}
   */
  public <C> ConnectionManagementStrategy<C> getStrategy(ConnectionProvider<C> connectionProvider) {
    ConnectionManagementType managementType = getManagementType(connectionProvider);
    if (managementType == POOLING) {
      return pooling(connectionProvider);
    }
    if (managementType == CACHED) {
      return cached(connectionProvider);
    } else if (managementType == NONE) {
      return withoutManagement(connectionProvider);
    } else {
      throw new IllegalArgumentException("Unknown management type: " + managementType);
    }
  }

  private <C> ConnectionManagementStrategy<C> cached(ConnectionProvider<C> connectionProvider) {
    return new CachedConnectionManagementStrategy<>(connectionProvider, muleContext);
  }

  private <C> ConnectionManagementStrategy<C> withoutManagement(ConnectionProvider<C> connectionProvider) {
    return new NullConnectionManagementStrategy<>(connectionProvider, muleContext);
  }

  private <C> ConnectionManagementStrategy<C> pooling(ConnectionProvider<C> connectionProvider) {
    String ownerConfigName = "";
    PoolingProfile poolingProfile = defaultPoolingProfile;
    if (connectionProvider instanceof ConnectionProviderWrapper) {
      poolingProfile = ((ConnectionProviderWrapper) connectionProvider).getPoolingProfile().orElse(poolingProfile);
      ownerConfigName = ((ConnectionProviderWrapper<C>) connectionProvider).getOwnerConfigName().orElse("");
    }

    return poolingProfile.isDisabled() ? withoutManagement(connectionProvider)
        : new PoolingConnectionManagementStrategy<>(connectionProvider, poolingProfile,
                                                    (PoolingListener<C>) unwrapProviderWrapper(connectionProvider,
                                                                                               PoolingConnectionProvider.class),
                                                    muleContext, ownerConfigName);
  }

  private <C> ConnectionManagementType getManagementType(ConnectionProvider<C> connectionProvider) {
    ConnectionManagementType type = NONE;

    if (connectionProvider instanceof ConnectionProviderWrapper) {
      return ((ConnectionProviderWrapper<C>) connectionProvider).getConnectionManagementType();
    }

    if (connectionProvider instanceof PoolingConnectionProvider) {
      type = POOLING;
    } else if (connectionProvider instanceof CachedConnectionProvider) {
      type = CACHED;
    }

    return type;
  }
}
