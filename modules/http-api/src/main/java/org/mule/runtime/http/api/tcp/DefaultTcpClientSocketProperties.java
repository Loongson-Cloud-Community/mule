/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.tcp;

/**
 * Default implementation of {@link TcpClientSocketProperties}. Instances can only be obtained through a
 * {@link TcpClientSocketPropertiesBuilder}.
 */
class DefaultTcpClientSocketProperties implements TcpClientSocketProperties, TcpSocketProperties {

  protected Integer sendBufferSize;
  protected Integer receiveBufferSize;
  protected Integer clientTimeout;
  protected Boolean sendTcpNoDelay;
  protected Integer linger;
  protected Boolean keepAlive;
  private Integer connectionTimeout;

  DefaultTcpClientSocketProperties(Integer sendBufferSize, Integer receiveBufferSize, Integer clientTimeout,
                                   Boolean sendTcpNoDelay, Integer linger, Boolean keepAlive, Integer connectionTimeout) {
    this.sendBufferSize = sendBufferSize;
    this.receiveBufferSize = receiveBufferSize;
    this.clientTimeout = clientTimeout;
    this.sendTcpNoDelay = sendTcpNoDelay;
    this.linger = linger;
    this.keepAlive = keepAlive;
    this.connectionTimeout = connectionTimeout;
  }

  @Override
  public Integer getSendBufferSize() {
    return sendBufferSize;
  }

  @Override
  public Integer getReceiveBufferSize() {
    return receiveBufferSize;
  }

  @Override
  public Integer getClientTimeout() {
    return clientTimeout;
  }

  @Override
  public Boolean getSendTcpNoDelay() {
    return sendTcpNoDelay;
  }

  @Override
  public Integer getLinger() {
    return linger;
  }

  @Override
  public Boolean getKeepAlive() {
    return keepAlive;
  }

  @Override
  public Integer getConnectionTimeout() {
    return connectionTimeout;
  }
}
