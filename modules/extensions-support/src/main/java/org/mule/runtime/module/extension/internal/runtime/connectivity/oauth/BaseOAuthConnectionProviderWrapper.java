/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.connectivity.oauth;

import static java.util.Collections.unmodifiableMap;
import static org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.MAX_REFRESH_ATTEMPTS;
import static org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.refreshTokenIfNecessary;
import static org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.validateOAuthConnection;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.core.internal.connection.ConnectionProviderWrapper;
import org.mule.runtime.core.internal.connection.ReconnectableConnectionProviderWrapper;
import org.mule.runtime.core.internal.retry.ReconnectionConfig;
import org.mule.oauth.client.api.state.ResourceOwnerOAuthContext;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Base class for an OAuth enabled {@link ConnectionProviderWrapper}.
 *
 * @param <C> the generic type of the returned connection
 * @since 4.3.0
 */
public abstract class BaseOAuthConnectionProviderWrapper<C> extends ReconnectableConnectionProviderWrapper<C> implements
    OAuthConnectionProviderWrapper<C> {

  protected final Map<Field, String> callbackValues;

  public BaseOAuthConnectionProviderWrapper(ConnectionProvider<C> delegate,
                                            ReconnectionConfig reconnectionConfig,
                                            Map<Field, String> callbackValues) {
    super(delegate, reconnectionConfig);
    this.callbackValues = unmodifiableMap(callbackValues);
  }

  @Override
  public ConnectionValidationResult validate(C connection) {
    return validateOAuthConnection(this, connection, getContext());
  }

  @Override
  public String getResourceOwnerId() {
    return getContext().getResourceOwnerId();
  }

  protected abstract ResourceOwnerOAuthContext getContext();
}
