/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.message;

/**
 * Base contract for a context object that keeps feature-specific state as part of the state of a given {@link InternalEvent}.
 * <p>
 * Implementations have no specific restriction about mutability. Each implementation can take the decision it best sees fit.
 * Important restriction however is that copies of each instance <b>MUST</b> be done through the {@link #copy()} method. Each
 * implementation must implement its own copying logic there and is free to use shallow or deep copying as needed.
 *
 * @param <T> the generic type of the specific implementation
 * @since 4.3.0
 */
public interface EventInternalContext<T extends EventInternalContext> {

  /**
   * Makes a copy of the given {@code context} by delegating into its {@link #copy()} method.
   * <p>
   * If {@code context} is {@code null} then {@code null} is returned.
   *
   * @param context a nullable context to be copied
   * @return a copy or {@code null}
   */
  static <M extends EventInternalContext> M copyOf(EventInternalContext<M> context) {
    return context != null ? context.copy() : null;
  }

  /**
   * @return a copy of {@code this} instance.
   */
  T copy();
}
