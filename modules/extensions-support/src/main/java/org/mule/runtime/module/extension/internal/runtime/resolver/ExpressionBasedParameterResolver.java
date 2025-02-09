/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static java.util.Optional.ofNullable;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.util.Optional;

/**
 * {@link ParameterResolver} implementation for the parameters that are resolved from an expression
 *
 * @param <T> Concrete parameter type to be resolved
 * @since 4.0
 */
class ExpressionBasedParameterResolver<T> implements ParameterResolver<T> {

  private final String expression;
  private final ValueResolvingContext context;
  private final TypeSafeExpressionValueResolver<T> valueResolver;

  ExpressionBasedParameterResolver(String expression,
                                   TypeSafeExpressionValueResolver<T> valueResolver,
                                   ValueResolvingContext context) {
    this.expression = expression;
    this.valueResolver = valueResolver;
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T resolve() {
    try {
      return valueResolver.resolve(context);
    } catch (MuleException e) {
      throw new MuleRuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getExpression() {
    return ofNullable(expression);
  }
}
