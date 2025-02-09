/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.server;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.HttpConstants.Protocol;
import org.mule.runtime.http.api.server.ws.WebSocketHandler;
import org.mule.runtime.http.api.server.ws.WebSocketHandlerManager;

import java.io.IOException;
import java.util.Collection;

/**
 * Represents a ServerSocket connection. Notice it should be started to be bound, stopped to be unbound and finally disposed to
 * release all related components. To extends it's functionality and not depend on API changes, this object should be used
 * internally instead of decorated.
 *
 * @since 4.0
 */
@NoImplement
public interface HttpServer {

  /**
   * Binds the ServerSocket to the network interface and starts listening for requests.
   *
   * @return this server
   * @throws IOException if there was a problem binding to the host and port specified.
   */
  HttpServer start() throws IOException;

  /**
   * Unbinds the ServerSocket to the network interface and stops listening for requests.
   * <p>
   * Currently executing requests are not affected and may proceed normally until {@link #dispose()} is called.
   *
   * @return this server
   */
  HttpServer stop();

  /**
   * Removes all secondary data to get rid of the server. Cannot be undone.
   * <p>
   * If there are any currently running requests, this method will block until either those requests are finished or a timeout
   * elapses. The requests that were received but didn't start processing, will be finished returning a 503 status code.
   */
  void dispose();

  /**
   * @return the server address this server is listening on.
   */
  ServerAddress getServerAddress();

  /**
   * @return the protocol (HTTP or HTTPS) this server is expecting.
   */
  Protocol getProtocol();

  /**
   * @return true if the server is currently stopping, false if it's stopped already or if it's not doing stop.
   */
  boolean isStopping();

  /**
   * @return true if the server is stopped, false otherwise.
   */
  boolean isStopped();

  /**
   * Enable TLS dynamically on this server using the supplied TLS context factory
   *
   * @param tlsContextFactory The TLS context factory to be used when dynamically enabled TLS at this server
   */
  default void enableTls(TlsContextFactory tlsContextFactory) {
    throw new UnsupportedOperationException("TLS is not supported");
  }

  /**
   * Disable TLS dynamically. It should work for when the TLS was dynamically enabled or even when the TLS was statically
   * configured in the listener configuration section of the application.
   */
  default void disableTls() {
    throw new UnsupportedOperationException("TLS is not supported");
  }

  /**
   * Adds a {@link RequestHandler} on the given path and for the given methods.
   *
   * @param methods        a list of methods to match
   * @param path           the path to match
   * @param requestHandler the handler to execute upon a matching request
   * @return a {@link RequestHandlerManager} for the handler
   */
  RequestHandlerManager addRequestHandler(final Collection<String> methods, final String path,
                                          final RequestHandler requestHandler);

  /**
   * Adds a {@link RequestHandler} on the given path and for all methods.
   *
   * @param path           the path to match
   * @param requestHandler the handler to execute upon a matching request
   * @return a {@link RequestHandlerManager} for the handler
   */
  RequestHandlerManager addRequestHandler(final String path, final RequestHandler requestHandler);

  /**
   * Adds a {@link WebSocketHandler}
   *
   * @param handler the handler
   * @return a {@link WebSocketHandlerManager}
   * @since 4.2.0
   */
  default WebSocketHandlerManager addWebSocketHandler(WebSocketHandler handler) {
    throw new UnsupportedOperationException("WebSockets are only supported in Enterprise Edition");
  }
}
