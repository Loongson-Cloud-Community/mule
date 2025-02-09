/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.oauth.api.listener;

import org.mule.api.annotation.Experimental;
import org.mule.api.annotation.NoImplement;
import org.mule.oauth.client.api.listener.OAuthStateListener;
import org.mule.oauth.client.api.state.ResourceOwnerOAuthContext;

/**
 * Allows to get notified about certain events related to Platform Managed OAuth tokens
 * <p>
 * Platform Managed OAuth is an experimental feature. It will only be enabled on selected environments and scenarios. Backwards
 * compatibility is not guaranteed.
 *
 * @since 4.3.0
 */
@Experimental
@NoImplement
public interface PlatformManagedOAuthStateListener extends OAuthStateListener {

  /**
   * Invoked each time an access token has been obtained from the platform
   *
   * @param context the resulting {@link ResourceOwnerOAuthContext}
   */
  default void onAccessToken(ResourceOwnerOAuthContext context) {

  }

  /**
   * Invoked each time a refresh token operation has been completed successfully
   *
   * @param context the resulting {@link ResourceOwnerOAuthContext}
   */
  default void onTokenRefreshed(ResourceOwnerOAuthContext context) {

  }
}
