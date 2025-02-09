/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.function;

import org.mule.runtime.api.el.ExpressionFunction;
import org.mule.runtime.api.meta.model.function.FunctionModel;

/**
 * Factory for creating {@link FunctionExecutor}s based on a given {@link FunctionModel}
 *
 * @since 4.0
 */
public interface FunctionExecutorFactory {

  /**
   * Creates a new {@link ExpressionFunction} based on a given {@link FunctionModel}
   *
   * @param functionModel the model of the function to be executed
   * @return a new {@link ExpressionFunction}
   */
  FunctionExecutor createExecutor(FunctionModel functionModel,
                                  FunctionParameterDefaultValueResolverFactory defaultResolverFactory);
}
