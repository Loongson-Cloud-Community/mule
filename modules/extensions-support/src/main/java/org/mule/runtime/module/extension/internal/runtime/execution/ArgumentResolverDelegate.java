/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.execution;

import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;
import org.mule.runtime.module.extension.internal.runtime.resolver.ArgumentResolver;

import java.util.function.Supplier;

/**
 * Extracts argument values from an {@link ExecutionContext} and exposes them as an array
 *
 * @since 3.7.0
 */
public interface ArgumentResolverDelegate {

  /**
   * @return An array with the {@link ArgumentResolver resolvers} used for resolving parameters
   * @since 4.3.0
   */
  ArgumentResolver<?>[] getArgumentResolvers();

  /**
   * Returns an object array with the argument values of the given {@code executionContext}
   *
   * @param executionContext the {@link ExecutionContext context} of an {@link ComponentModel} being currently executed
   * @param parameterTypes   each argument's type
   * @return an object array
   */
  Object[] resolve(ExecutionContext executionContext, Class<?>[] parameterTypes);

  /**
   * Returns an array of {@link Supplier} of the argument values of the given {@code executionContext}.
   * <p>
   * Actual resolution of each argument is deferred until the {@link Supplier#get()} method is invoked on each supplier
   *
   * @param executionContext the {@link ExecutionContext context} of an {@link ComponentModel} being currently executed
   * @param parameterTypes   each argument's type
   * @return a {@link Supplier} array
   * @since 4.3.0
   */
  Supplier<Object>[] resolveDeferred(ExecutionContext executionContext, Class<?>[] parameterTypes);
}
