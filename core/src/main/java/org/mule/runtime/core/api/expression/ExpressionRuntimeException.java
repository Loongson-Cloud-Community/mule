/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.expression;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessage;

/**
 * Thrown by the {@link org.mule.runtime.core.api.el.ExpressionManager} when a failure occurs evaluating an expression.
 */
@NoExtend
public class ExpressionRuntimeException extends MuleRuntimeException {

  private static final long serialVersionUID = -8632366166228091959L;

  /**
   * @param message the exception message
   */
  public ExpressionRuntimeException(I18nMessage message) {
    super(message);
  }

  /**
   * @param message the exception message
   * @param cause   the exception that triggered this exception
   */
  public ExpressionRuntimeException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }
}
