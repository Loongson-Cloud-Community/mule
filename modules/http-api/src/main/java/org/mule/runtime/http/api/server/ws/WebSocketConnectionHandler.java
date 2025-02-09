/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.server.ws;

import org.mule.runtime.http.api.ws.WebSocket;
import org.mule.runtime.http.api.ws.WebSocketCloseCode;

/**
 * Handles connection aspects of an inbound {@link WebSocket}
 *
 * @since 4.2.0
 */
public interface WebSocketConnectionHandler {

  /**
   * Generates the id of the socket that will be created from the given {@code request}
   *
   * @param request the inbound request to open a {@link WebSocket}
   * @return an id. Cannot be null or empty.
   */
  String getSocketId(WebSocketRequest request);

  /**
   * Invoked when the {@code socket} is established
   *
   * @param socket  the established {@link WebSocket}
   * @param request the inbound {@link WebSocketRequest request}
   * @throws WebSocketConnectionRejectedException if the connection is refused by the owning server
   */
  void onConnect(WebSocket socket, WebSocketRequest request) throws WebSocketConnectionRejectedException;

  /**
   * Invoked when the {@code socket} is closed. This method will be invoked whether the socket was closed locally or remotely.
   *
   * @param socket    the established {@link WebSocket}
   * @param request   the inbound {@link WebSocketRequest request}
   * @param closeCode the close code used
   * @param reason    the reason provided
   */
  void onClose(WebSocket socket, WebSocketRequest request, WebSocketCloseCode closeCode, String reason);
}
