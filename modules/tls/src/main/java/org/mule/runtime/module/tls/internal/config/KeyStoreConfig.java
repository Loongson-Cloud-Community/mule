/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.tls.internal.config;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.tls.TlsContextKeyStoreConfiguration;

/**
 * POJO for parsing the TLS key store before building the actual DefaultTlsContext
 *
 * @since 4.0
 */
public class KeyStoreConfig extends AbstractComponent implements TlsContextKeyStoreConfiguration {

  private String alias;
  private String keyPassword;
  private String path;
  private String password;
  private String type;
  private String algorithm;

  @Override
  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public String getKeyPassword() {
    return keyPassword;
  }

  public void setKeyPassword(String keyPassword) {
    this.keyPassword = keyPassword;
  }

  @Override
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }
}
