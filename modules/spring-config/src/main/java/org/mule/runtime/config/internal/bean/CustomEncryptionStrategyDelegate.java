/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.bean;

import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.meta.NameableObject;
import org.mule.runtime.core.api.security.CryptoFailureException;
import org.mule.runtime.core.api.security.EncryptionStrategy;

import java.io.InputStream;

/**
 * Delegate for encryption strategies configured inside security-manager
 *
 * @since 4.0
 */
public class CustomEncryptionStrategyDelegate extends AbstractComponent implements EncryptionStrategy {

  private EncryptionStrategy delegate;
  private String name;

  public CustomEncryptionStrategyDelegate(EncryptionStrategy delegate, String name) {
    this.delegate = delegate;
    if (delegate instanceof NameableObject) {
      ((NameableObject) delegate).setName(name);
    }
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void initialise() throws InitialisationException {
    delegate.initialise();
  }

  @Override
  public InputStream encrypt(InputStream data, Object info) throws CryptoFailureException {
    return delegate.encrypt(data, info);
  }

  @Override
  public InputStream decrypt(InputStream data, Object info) throws CryptoFailureException {
    return delegate.decrypt(data, info);
  }

  @Override
  public byte[] encrypt(byte[] data, Object info) throws CryptoFailureException {
    return delegate.encrypt(data, info);
  }

  @Override
  public byte[] decrypt(byte[] data, Object info) throws CryptoFailureException {
    return delegate.decrypt(data, info);
  }
}
