/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.oauth;

import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.connectivity.oauth.AuthorizationCode;
import org.mule.sdk.api.connectivity.oauth.AccessTokenExpiredException;

@AuthorizationCode(accessTokenUrl = TestOAuthConnectionProvider.ACCESS_TOKEN_URL,
    authorizationUrl = TestOAuthConnectionProvider.AUTH_URL,
    defaultScopes = TestOAuthConnectionProvider.DEFAULT_SCOPE)
@Alias("validated-connection")
public class TestOAuthRefreshValidationConnectionProvider extends TestOAuthConnectionProvider {

  @Override
  public ConnectionValidationResult validate(TestOAuthConnection connection) {
    if (!connection.getState().getState().getAccessToken().contains("refreshed")) {
      return failure("Token is expired!", new AccessTokenExpiredException());
    }
    return success();
  }
}
