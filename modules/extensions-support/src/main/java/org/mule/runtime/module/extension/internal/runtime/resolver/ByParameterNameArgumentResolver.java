/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;

/**
 * An implementation of {@link ArgumentResolver} which resolves to a parameter value of name {@link #parameterName}
 *
 * @param <T> the type of the argument to be resolved
 * @since 3.7.0
 */
public class ByParameterNameArgumentResolver<T> implements ArgumentResolver<T> {

  private final String parameterName;

  public ByParameterNameArgumentResolver(String parameterName) {
    this.parameterName = parameterName;
  }

  /**
   * {@inheritDoc}
   *
   * @param executionContext an {@link ExecutionContext}
   * @return the result of invoking {@link ExecutionContext#getParameter(String)} with {@link #parameterName}
   */
  @Override
  public T resolve(ExecutionContext executionContext) {
    return (T) executionContext.getParameters().get(parameterName);
  }

  @Override
  public String toString() {
    return "ByParameterNameArgumentResolver(" + parameterName + ")";
  }
}
