/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.service.Service;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientFactory;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.runtime.http.api.server.HttpServerFactory;
import org.mule.runtime.http.api.utils.RequestMatcherRegistry;
import org.mule.runtime.http.api.utils.RequestMatcherRegistry.RequestMatcherRegistryBuilder;
import org.mule.runtime.http.api.ws.WebSocketBroadcaster;

/**
 * Provides HTTP server and client factories.
 *
 * @since 4.0
 */
@NoImplement
public interface HttpService extends Service {

  /**
   * @return an {@link HttpServerFactory} capable of creating {@link HttpServer}s.
   */
  HttpServerFactory getServerFactory();

  /**
   * @return an {@link HttpClientFactory} capable of creating {@link HttpClient}s.
   */
  HttpClientFactory getClientFactory();

  /**
   * @return a fresh builder of {@link RequestMatcherRegistry RequestMatcherRegistries}.
   * @since 4.1.5
   */
  RequestMatcherRegistryBuilder getRequestMatcherRegistryBuilder();

  /**
   * Returns a new {@link WebSocketBroadcaster}. Instances are not be assumed reusable. Unless specific implementation says
   * otherwise, create a new broadcaster per each message to be broadcasted.
   *
   * @return A new {@link WebSocketBroadcaster}
   * @since 4.2.0
   */
  default WebSocketBroadcaster newWebSocketBroadcaster() {
    throw new UnsupportedOperationException("WebSockets are only supported in Enterprise Edition");
  }

}
