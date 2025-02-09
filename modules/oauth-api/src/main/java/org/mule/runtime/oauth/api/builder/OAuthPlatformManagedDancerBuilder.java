/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.oauth.api.builder;

import org.mule.api.annotation.Experimental;
import org.mule.api.annotation.NoImplement;
import org.mule.oauth.client.api.builder.OAuthDancerBuilder;
import org.mule.runtime.oauth.api.PlatformManagedOAuthDancer;
import org.mule.runtime.oauth.api.listener.PlatformManagedOAuthStateListener;

/**
 * Builder that allows to configure the attributes for the {@link PlatformManagedOAuthDancer}
 * <p>
 * Platform Managed OAuth is an experimental feature. It will only be enabled on selected environments and scenarios. Backwards
 * compatibility is not guaranteed.
 *
 * @since 4.3.0
 */
@NoImplement
@Experimental
public interface OAuthPlatformManagedDancerBuilder extends OAuthDancerBuilder<PlatformManagedOAuthDancer> {

  /**
   * Sets the URI that identifies the connection that is defined in the Anypoint Platform
   *
   * @param connectionUri the id of the connection which token we want to obtain
   * @return {@code this} builder
   */
  OAuthPlatformManagedDancerBuilder connectionUri(String connectionUri);

  /**
   * Sets the ID of the organization that defined the connection in the Anypoint Platform
   *
   * @param organizationId an organizationId
   * @return {@code this} builder
   */
  OAuthPlatformManagedDancerBuilder organizationId(String organizationId);

  /**
   * Sets the url of the platform API that serves the managed tokens
   *
   * @param platformUrl the url of the platform API that serves the managed tokens
   * @return {@code this} builder
   */
  OAuthPlatformManagedDancerBuilder platformUrl(String platformUrl);

  /**
   * Sets the platform API version to a specific one.
   *
   * @param apiVersion the platform API version
   * @return {@code this} builder
   * @since 4.4.0
   */
  default OAuthPlatformManagedDancerBuilder apiVersion(String apiVersion) {
    return this;
  }

  /**
   * Adds the {@code listener}. Listeners will be invoked in the same order as they were added
   *
   * @param listener the {@link PlatformManagedOAuthStateListener} to be added
   * @throws IllegalArgumentException if the {@code listener} is {@code null}
   */
  OAuthPlatformManagedDancerBuilder addListener(PlatformManagedOAuthStateListener listener);
}
