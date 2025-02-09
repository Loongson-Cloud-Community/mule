/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.client;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.client.proxy.ProxyConfig;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.util.Optional;

/**
 * Options for setting up an {@link HttpRequest}. Instances can only be obtained through a {@link HttpRequestOptionsBuilder}.
 *
 * @since 4.2
 */
@NoImplement
public interface HttpRequestOptions {

  /**
   * @return a fresh {@link HttpRequestOptionsBuilder} to create instances.
   */
  static HttpRequestOptionsBuilder builder() {
    return new HttpRequestOptionsBuilder();
  }

  /**
   * @param options {@link HttpRequestOptions} to set up builder with.
   * @return a fresh {@link HttpRequestOptionsBuilder} to create instances.
   */
  static HttpRequestOptionsBuilder builder(HttpRequestOptions options) {
    return new HttpRequestOptionsBuilder(options);
  }

  /**
   * @return the time (in milliseconds) to wait for a response
   */
  int getResponseTimeout();

  /**
   * @return whether or not to follow redirect responses
   */
  boolean isFollowsRedirect();

  /**
   * @return the {@link HttpAuthentication} to use, if any.
   */
  Optional<HttpAuthentication> getAuthentication();

  /**
   * @return if the request should contain a body even for methods without body semantics (i.e. GET, DELETE, TRACE, OPTIONS and
   *         HEAD, see <a href="https://www.rfc-editor.org/rfc/rfc7231">RFC 7231</a>).
   */
  boolean shouldSendBodyAlways();

  /**
   * @return the {@link ProxyConfig} to use, if any.
   */
  Optional<ProxyConfig> getProxyConfig();

}
