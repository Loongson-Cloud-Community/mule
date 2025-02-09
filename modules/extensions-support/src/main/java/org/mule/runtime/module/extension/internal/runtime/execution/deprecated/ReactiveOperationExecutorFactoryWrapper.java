/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.execution.deprecated;

import static java.util.Collections.emptyMap;

import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.api.meta.model.construct.ConstructModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.extension.api.runtime.operation.ComponentExecutor;
import org.mule.runtime.extension.api.runtime.operation.ComponentExecutorFactory;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;
import org.mule.runtime.module.extension.internal.runtime.execution.OperationArgumentResolverFactory;

import java.util.Map;
import java.util.function.Function;

/**
 * Decorates a {@link ComponentExecutorFactory} so that non blocking operations can correctly hook into the execution pipeline.
 *
 * @since 4.0
 * @deprecated since 4.2
 */
@Deprecated
public final class ReactiveOperationExecutorFactoryWrapper<T extends ComponentModel>
    implements ComponentExecutorFactory<T>, OperationArgumentResolverFactory<T> {

  private final ComponentExecutorFactory delegate;

  /**
   * Creates a new instance
   *
   * @param delegate the {@link ComponentExecutorFactory} to be decorated
   */
  public ReactiveOperationExecutorFactoryWrapper(ComponentExecutorFactory<T> delegate) {
    this.delegate = delegate;
  }

  /**
   * @return a {@link ReactiveInterceptableOperationExecutorWrapper} which decorates the result of propagating this invocation to
   *         the {@link #delegate}
   */

  @Override
  public ComponentExecutor<T> createExecutor(T componentModel, Map<String, Object> parameters) {
    ComponentExecutor<T> executor = delegate.createExecutor(componentModel, parameters);
    if (isJavaNonBlocking(componentModel, executor)) {
      executor = new ReactiveOperationExecutionWrapper(executor);
    }

    return executor;
  }

  private boolean isJavaNonBlocking(T componentModel, ComponentExecutor<T> executor) {
    if (componentModel instanceof OperationModel && !((OperationModel) componentModel).isBlocking()) {
      return executor instanceof ReactiveMethodOperationExecutor;
    } else {
      return componentModel instanceof ConstructModel;
    }
  }

  @Override
  public Function<ExecutionContext<T>, Map<String, Object>> createArgumentResolver(T componentModel) {
    return delegate instanceof OperationArgumentResolverFactory
        ? ((OperationArgumentResolverFactory) delegate).createArgumentResolver(componentModel)
        : ec -> emptyMap();
  }
}
