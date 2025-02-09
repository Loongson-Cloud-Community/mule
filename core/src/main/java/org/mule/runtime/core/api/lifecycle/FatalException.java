/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.lifecycle;

import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.lifecycle.LifecycleException;

/**
 * <code>FatalException</code> can be thrown during initialisation or during execution to indicate that something fatal has
 * occurred and the MuleManager must shutdown.
 */
public class FatalException extends LifecycleException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -330442983239074935L;

  /**
   * @param message   the exception message
   * @param component the object that failed during a lifecycle method call
   */
  public FatalException(I18nMessage message, Object component) {
    super(message, component);
  }

  /**
   * @param message   the exception message
   * @param cause     the exception that cause this exception to be thrown
   * @param component the object that failed during a lifecycle method call
   */
  public FatalException(I18nMessage message, Throwable cause, Object component) {
    super(message, cause, component);
  }

  /**
   * @param cause     the exception that cause this exception to be thrown
   * @param component the object that failed during a lifecycle method call
   */
  public FatalException(Throwable cause, Object component) {
    super(cause, component);
  }
}
