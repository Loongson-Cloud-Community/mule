/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.util.attribute;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManagerSession;
import org.mule.runtime.core.api.el.ExtendedExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;

/**
 * Delegate evaluator contract for configuration attributes.
 *
 * @since 4.2.0
 */
public interface AttributeEvaluatorDelegate<T> {

  /**
   * Resolves the attribute's value.
   *
   * @param event   the current event being processed.
   * @param manager the expression manager used to evaluate the values.
   * @return a new resolved {@link TypedValue}.
   */
  TypedValue<T> resolve(CoreEvent event, ExtendedExpressionManager manager);

  /**
   * Resolves the attribute's value using a pre-constructed session that caches a set of bindings.
   *
   * @param session the expression manager used to evaluate the values.
   * @return a new resolved {@link TypedValue}.
   */
  TypedValue<T> resolve(ExpressionManagerSession session);

  /**
   * Resolves the attribute's value using a {@link BindingContext}, for compatibility purposes.
   *
   * @return a new resolved {@link TypedValue}.
   * @deprecated added for compatibility purposes, use {@link #resolve(ExpressionManagerSession)}.
   */
  TypedValue<T> resolve(BindingContext context, ExtendedExpressionManager expressionManager);
}
