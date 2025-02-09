/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util.func;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Utilities for scenarios in which a given task should be executed only once.
 *
 * @since 4.0
 */
public class Once {

  /**
   * Creates a new {@link RunOnce}
   *
   * @param runnable the delegate to be executed only once
   * @return a new instance
   */
  public static RunOnce of(CheckedRunnable runnable) {
    return new RunOnce(runnable);
  }

  /**
   * Creates a new {@link ConsumeOnce}
   *
   * @param consumer the delegate to be executed only once
   * @return a new instance
   */
  public static <T> ConsumeOnce<T> of(CheckedConsumer<T> consumer) {
    return new ConsumeOnce<>(consumer);
  }

  /**
   * Executes a given {@link CheckedConsumer} only once.
   * <p>
   * Once the {@link #consumeOnce(Object)} method has been successfully executed, subsequent invocations to such method will have
   * no effect, even if the supplied value is different. Notice that the key word here is {@code successfully}. If the method
   * fails, each invocation to {@link #consumeOnce(Object)} WILL run the delegate until it completes successfully.
   * <p>
   * Instances are thread safe, which means that if two threads are competing for the first successful invocation, only one will
   * prevail and the other one will get a no-op execution.
   *
   * @since 4.0
   */
  public static class ConsumeOnce<T> extends AbstractOnce {

    private CheckedConsumer<T> consumer;

    private ConsumeOnce(CheckedConsumer<T> delegate) {
      consumer = delegate;
    }

    public void consumeOnce(T value) {
      if (!done) {
        synchronized (this) {
          if (!done) {
            consumer.accept(value);
            done = true;
            consumer = null;
          }
        }
      }
    }
  }

  /**
   * Executes a given {@link CheckedRunnable} only once.
   * <p>
   * Once the {@link #runOnce()} method has been successfully executed, subsequent invocations to such method will have no effect.
   * Notice that the key word here is {@code successfully}. If the method fails, each invocation to {@link #runOnce()} WILL run
   * the delegate until it completes successfully.
   * <p>
   * Instances are thread safe, which means that if two threads are competing for the first successful invocation, only one will
   * prevail and the other one will get a no-op execution.
   *
   * @since 4.0
   */
  public static class RunOnce extends AbstractOnce {

    private CheckedRunnable runner;

    private RunOnce(CheckedRunnable delegate) {
      runner = delegate;
    }

    /**
     * Runs (or not) the delegate according to the behaviour described on the class javadoc
     */
    public void runOnce() {
      if (!done) {
        synchronized (this) {
          if (!done) {
            runner.run();
            done = true;
            runner = null;
          }
        }
      }
    }
  }

  private static abstract class AbstractOnce {

    protected final ReentrantLock lock = new ReentrantLock();
    protected volatile boolean done = false;
  }

  private Once() {}
}
