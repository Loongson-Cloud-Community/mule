/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.processor.strategy;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.lang.Thread.currentThread;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static org.slf4j.helpers.NOPLogger.NOP_LOGGER;

import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.core.api.construct.BackPressureReason;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Sink;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;

/**
 * {@link Sink} implementation that creates and caches a single {@link DirectSink} for each callee thread.
 */
class PerThreadSink implements Sink, Disposable {

  private final Supplier<Sink> sinkSupplier;
  private final Cache<Thread, Sink> sinkCache =
      newBuilder().weakValues().removalListener(notification -> disposeIfNeeded(notification.getValue(), NOP_LOGGER)).build();

  /**
   * Create a {@link PerThreadSink} that will create and use a given {@link Sink} for each distinct caller {@link Thread}.
   *
   * @param sinkSupplier {@link Supplier} for the {@link Sink} that sould be used for each thread.
   */
  public PerThreadSink(Supplier<Sink> sinkSupplier) {
    this.sinkSupplier = sinkSupplier;
  }

  @Override
  public void accept(CoreEvent event) {
    try {
      sinkCache.get(currentThread(), () -> sinkSupplier.get()).accept(event);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Unable to create Sink for Thread " + currentThread(), e.getCause());
    }
  }

  @Override
  public BackPressureReason emit(CoreEvent event) {
    try {
      return sinkCache.get(currentThread(), () -> sinkSupplier.get()).emit(event);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Unable to create Sink for Thread " + currentThread(), e.getCause());
    }
  }

  @Override
  public void dispose() {
    disposeIfNeeded(sinkCache.asMap().entrySet(), NOP_LOGGER);
    sinkCache.invalidateAll();
  }

}
