/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import reactor.core.publisher.FluxSink;

/**
 * Provides a round-robin access to the internally kept {@link FluxSink}s built with factory function.
 *
 * @param <T> the value type
 *
 * @since 4.2
 */
public class RoundRobinFluxSinkSupplier<T> implements FluxSinkSupplier<T> {

  private final List<FluxSink<T>> fluxSinks;
  private final AtomicInteger index = new AtomicInteger(0);
  // Saving update function to avoid creating the lambda every time
  private final IntUnaryOperator update;

  public RoundRobinFluxSinkSupplier(int size, Supplier<FluxSink<T>> sinkFactory) {
    final List<FluxSink<T>> sinks = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      sinks.add(sinkFactory.get());
    }
    this.fluxSinks = sinks;
    this.update = (value) -> (value + 1) % size;
  }

  private int nextIndex() {
    return index.getAndUpdate(update);
  }

  @Override
  public void dispose() {
    fluxSinks.forEach(FluxSink::complete);
  }

  @Override
  public FluxSink<T> get() {
    return fluxSinks.get(nextIndex());
  }

}
