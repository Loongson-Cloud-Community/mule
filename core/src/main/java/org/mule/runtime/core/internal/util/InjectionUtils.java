/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util;

import org.mule.runtime.core.internal.registry.InjectionTargetDecorator;

/**
 * Utilities for dependency and values injection
 *
 * @since 4.5.0
 */
public final class InjectionUtils {

  private InjectionUtils() {}

  /**
   * Recursively tests the given {@code object} to be an instance of {@link InjectionTargetDecorator} and returns the actual
   * instance in which injection is to happen
   *
   * @param target the injection target
   * @param <T>    the target's generic type
   * @return the actual injection target
   * @since 4.5.0
   */
  public static <T> T getInjectionTarget(T target) {
    while (target instanceof InjectionTargetDecorator) {
      target = ((InjectionTargetDecorator<T>) target).getDelegate();
    }

    return target;
  }
}
