/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.authcode;

import static java.util.Optional.ofNullable;
import org.mule.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;

import java.util.Optional;

/**
 * Immutable implementation of {@link AuthorizationCodeState}
 *
 * @see AuthorizationCodeState
 * @since 4.0
 */
public class ImmutableAuthorizationCodeState implements AuthorizationCodeState {

  private final String accessToken;
  private final String refreshToken;
  private final String resourceOwnerId;
  private final String expiresIn;
  private final String state;
  private final String authorizationUrl;
  private final String accessTokenUrl;
  private final Optional<String> externalCallbackUrl;
  private final String consumerKey;
  private final String consumerSecret;

  public ImmutableAuthorizationCodeState(String accessToken,
                                         String refreshToken,
                                         String resourceOwnerId,
                                         String expiresIn,
                                         String state,
                                         String authorizationUrl,
                                         String accessTokenUrl,
                                         Optional<String> externalCallbackUrl,
                                         String consumerKey,
                                         String consumerSecret) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.resourceOwnerId = resourceOwnerId;
    this.expiresIn = expiresIn;
    this.state = state;
    this.authorizationUrl = authorizationUrl;
    this.accessTokenUrl = accessTokenUrl;
    this.externalCallbackUrl = externalCallbackUrl;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public Optional<String> getRefreshToken() {
    return ofNullable(refreshToken);
  }

  @Override
  public String getResourceOwnerId() {
    return resourceOwnerId;
  }

  @Override
  public Optional<String> getExpiresIn() {
    return ofNullable(expiresIn);
  }

  @Override
  public Optional<String> getState() {
    return ofNullable(state);
  }

  @Override
  public String getAuthorizationUrl() {
    return authorizationUrl;
  }

  @Override
  public String getAccessTokenUrl() {
    return accessTokenUrl;
  }

  @Override
  public String getConsumerKey() {
    return consumerKey;
  }

  @Override
  public String getConsumerSecret() {
    return consumerSecret;
  }

  @Override
  public Optional<String> getExternalCallbackUrl() {
    return externalCallbackUrl;
  }
}
