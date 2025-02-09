/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.authcode;

import static java.util.Optional.ofNullable;

import java.util.Optional;

/**
 * Groups the sum of all the parameters that a user configured in order to provision an OAuth access token callback
 *
 * @since 4.0
 */
public class OAuthCallbackConfig {

  private final String listenerConfig;
  private final String callbackPath;
  private final String localAuthorizePath;
  private final String externalCallbackUrl;

  public OAuthCallbackConfig(String listenerConfig, String callbackPath, String localAuthorizePath, String externalCallbackUrl) {
    this.listenerConfig = listenerConfig;
    this.callbackPath = callbackPath;
    this.localAuthorizePath = localAuthorizePath;
    this.externalCallbackUrl = externalCallbackUrl;
  }

  public String getListenerConfig() {
    return listenerConfig;
  }

  public String getCallbackPath() {
    return callbackPath;
  }

  public String getLocalAuthorizePath() {
    return localAuthorizePath;
  }

  public Optional<String> getExternalCallbackUrl() {
    return ofNullable(externalCallbackUrl);
  }
}
