/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.domain;

/**
 * Represents the HTTP message protocol.
 *
 * @since 4.0
 */
public enum HttpProtocol {

  HTTP_0_9("HTTP/0.9"), HTTP_1_0("HTTP/1.0"), HTTP_1_1("HTTP/1.1");

  private final String protocolName;

  HttpProtocol(String protocolName) {
    this.protocolName = protocolName;
  }

  public String asString() {
    return protocolName;
  }
}
