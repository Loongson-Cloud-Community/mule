/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.security;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.i18n.I18nMessageFactory;

/**
 * {@code CryptoFailureException} is a generic exception thrown by an CryptoStrategy if encryption or decryption fails. The
 * constructors of this exception accept a {@link EncryptionStrategy} that will be included in the exception message. Implementors
 * of {@link EncryptionStrategy} should provide a toString method that exposes *only* information that maybe useful for debugging
 * <b>not</b> passwords, secret keys, etc.
 *
 * @since 4.0
 */
public final class CryptoFailureException extends MuleException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 1336343718508294381L;

  private transient EncryptionStrategy encryptionStrategy;

  public CryptoFailureException(I18nMessage message, EncryptionStrategy strategy) {
    super(message);
    String s = (strategy == null ? "null" : strategy.toString());
    addInfo("Encryption", s);
    this.encryptionStrategy = strategy;
  }

  public CryptoFailureException(I18nMessage message, EncryptionStrategy strategy, Throwable cause) {
    super(message, cause);
    String s = (strategy == null ? "null" : strategy.toString());
    addInfo("Encryption", s);
    this.encryptionStrategy = strategy;
  }

  public CryptoFailureException(EncryptionStrategy strategy, Throwable cause) {
    super(I18nMessageFactory.createStaticMessage("Crypto Failure"), cause);
    String s = (strategy == null ? "null" : strategy.toString());
    addInfo("Encryption", s);
    this.encryptionStrategy = strategy;

  }

  public EncryptionStrategy getEncryptionStrategy() {
    return encryptionStrategy;
  }
}
