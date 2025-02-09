/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util;

import org.mule.runtime.core.api.util.func.CheckedRunnable;
import org.mule.runtime.core.api.util.func.CheckedSupplier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

/**
 * Utilities for concurrency
 *
 * @since 4.0
 */
public class ConcurrencyUtils {

  /**
   * Safely releases the given {@code lock} without failing if it is in an illegal state
   *
   * @param lock a {@link Lock}
   */
  public static void safeUnlock(Lock lock) {
    try {
      lock.unlock();
    } catch (IllegalMonitorStateException e) {
      // lock was released early to improve performance and somebody else took it. This is fine
    }
  }

  /**
   * Returns the value of the given {@code supplier} between the boundaries of the given {@code lock}. It guarantees that the lock
   * is released
   *
   * @param lock     a {@link Lock}
   * @param supplier a {@link CheckedSupplier}
   * @param <T>      the generic type of the returned value
   * @return the supplied value
   * @deprecated since 4.3.0 on grounds of performance overhead. Handle this manually instead
   */
  @Deprecated
  public static <T> T withLock(Lock lock, CheckedSupplier<T> supplier) {
    lock.lock();
    try {
      return supplier.get();
    } finally {
      safeUnlock(lock);
    }
  }

  /**
   * Execute the given {@code delegate} between the boundaries of the given {@code lock}. It guarantees that the lock is released
   *
   * @param lock     a {@link Lock}
   * @param delegate a {@link CheckedRunnable}
   * @deprecated since 4.3.0 on grounds of performance overhead. Handle this manually instead
   */
  @Deprecated
  public static void withLock(Lock lock, CheckedRunnable delegate) {
    lock.lock();
    try {
      delegate.run();
    } finally {
      safeUnlock(lock);
    }
  }

  /**
   * Returns a {@link CompletableFuture} already exceptionally completed with the given {@code throwable}
   *
   * @param throwable the {@link Throwable} that completed the future
   * @param <T>       the future's generic type
   * @return an exceptionally completed future
   * @since 4.3.0
   */
  public static <T> CompletableFuture<T> exceptionallyCompleted(Throwable throwable) {
    CompletableFuture<T> f = new CompletableFuture<>();
    f.completeExceptionally(throwable);

    return f;
  }

  private ConcurrencyUtils() {}
}
