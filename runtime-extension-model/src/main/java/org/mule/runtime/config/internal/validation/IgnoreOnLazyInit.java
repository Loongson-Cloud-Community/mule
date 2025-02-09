/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.mule.runtime.ast.api.validation.Validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@link Validation} implementations annotated with this will not be evaluated when deploying with {@code lazyInit} enabled.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface IgnoreOnLazyInit {

  /**
   * Whether to force this validations via the deployment property
   * {@code mule.application.deployment.lazyInit.enableDslDeclarationValidations} even if running in lazy mode.
   */
  boolean forceDslDeclarationValidation() default false;
}
