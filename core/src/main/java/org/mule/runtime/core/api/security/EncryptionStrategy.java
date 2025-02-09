/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.security;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.meta.NamedObject;

import java.io.InputStream;

/**
 * {@code EncryptionStrategy} can be used to provide different types of Encryption strategy objects. These can be configured with
 * different information relevant with the encryption method being used, for example for Password Based Encryption (PBE) a
 * password, salt, iteration count and algorithm may be set on the strategy.
 *
 * @since 4.0
 */
@NoImplement
public interface EncryptionStrategy extends Initialisable, NamedObject {

  /**
   * Encrypts the given {@code data} using {@code this} encryption strategy
   *
   * @param data an {@link InputStream} providing the data to encrypt
   * @param info information relevant with the encryption method being used
   * @return an {@link InputStream} with the {@code data} encrypted
   * @throws CryptoFailureException if an error occurs while encrypting
   */
  InputStream encrypt(InputStream data, Object info) throws CryptoFailureException;

  /**
   * Decrypts the given {@code data} using {@code this} encryption strategy
   *
   * @param data an {@link InputStream} providing the data to decrypt
   * @param info information relevant with the encryption method used
   * @return an {@link InputStream} with the {@code data} decrypted
   * @throws CryptoFailureException if an error occurs while decrypting
   */
  InputStream decrypt(InputStream data, Object info) throws CryptoFailureException;

  /**
   * Encrypts the given {@code data} using {@code this} encryption strategy
   *
   * @param data the data to encrypt
   * @param info information relevant with the encryption method being used
   * @return the encrypted {@code data}
   * @throws CryptoFailureException if an error occurs while encrypting
   */
  byte[] encrypt(byte[] data, Object info) throws CryptoFailureException;

  /**
   * Decrypts the given {@code data} using {@code this} encryption strategy
   *
   * @param data the data to decrypt
   * @param info information relevant with the encryption method used
   * @return the decrypt {@code data}
   * @throws CryptoFailureException if an error occurs while decrypting
   */
  byte[] decrypt(byte[] data, Object info) throws CryptoFailureException;
}
