/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.ocs;

import static java.util.Optional.empty;

import org.mule.oauth.client.api.state.ResourceOwnerOAuthContext;
import org.mule.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;
import org.mule.runtime.oauth.api.PlatformManagedConnectionDescriptor;
import org.mule.runtime.oauth.api.PlatformManagedOAuthDancer;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of {@link AbstractPlatformOAuthStateAdapter} that implements {@link AuthorizationCodeState}
 *
 * @since 4.3.0
 */
public class PlatformAuthorizationCodeStateAdapter extends AbstractPlatformOAuthStateAdapter implements AuthorizationCodeState {

  private final PlatformManagedConnectionDescriptor descriptor;

  public PlatformAuthorizationCodeStateAdapter(PlatformManagedOAuthDancer dancer,
                                               PlatformManagedConnectionDescriptor descriptor,
                                               Consumer<ResourceOwnerOAuthContext> onUpdate) {
    super(dancer, onUpdate);
    this.descriptor = descriptor;
  }

  @Override
  public Optional<String> getRefreshToken() {
    return empty();
  }

  @Override
  public String getResourceOwnerId() {
    return descriptor.getDisplayName();
  }

  @Override
  public Optional<String> getState() {
    return empty();
  }

  @Override
  public String getAuthorizationUrl() {
    return "";
  }

  @Override
  public String getAccessTokenUrl() {
    return "";
  }

  @Override
  public String getConsumerKey() {
    return "";
  }

  @Override
  public String getConsumerSecret() {
    return "";
  }

  @Override
  public Optional<String> getExternalCallbackUrl() {
    return empty();
  }
}
