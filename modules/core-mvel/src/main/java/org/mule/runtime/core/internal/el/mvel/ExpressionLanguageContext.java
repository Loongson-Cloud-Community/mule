/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel;

import java.lang.reflect.Method;

/**
 * @since 3.3
 */
public interface ExpressionLanguageContext {

  void importClass(Class<?> clazz);

  void importClass(String name, Class<?> clazz);

  void importStaticMethod(String name, Method method);

  <T> void addVariable(String name, T value);

  <T> void addVariable(String name, T value, VariableAssignmentCallback<T> assignmentCallback);

  <T> void addFinalVariable(String name, T value);

  void addAlias(String alias, String expression);

  /**
   * Adds an alias that requires access to internal Mule API
   *
   * @param alias      name to be aliased
   * @param expression expression to replace the alias.
   */
  void addInternalAlias(String alias, String expression);

  void declareFunction(String name, ExpressionLanguageFunction function);

  <T> T getVariable(String name);

  <T> T getVariable(String name, Class<T> type);

  boolean contains(String name);
}
