/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.routing;

import org.mule.runtime.core.api.el.ExpressionManagerSession;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.internal.rx.FluxSinkRecorder;

import reactor.core.publisher.Flux;

/**
 * Composition of a {@link ProcessorRoute} and everything required to convert it into a reactor executable chain.
 *
 * @Since 4.3.0
 */
class ExecutableRoute {

  private final ProcessorRoute route;
  private final Flux<CoreEvent> publisher;
  private final FluxSinkRecorder<CoreEvent> sinkRecorder;

  ExecutableRoute(ProcessorRoute route) {
    this.route = route;
    sinkRecorder = new FluxSinkRecorder<>();
    publisher = sinkRecorder.flux()
        .transform(route.getProcessor());
  }

  /**
   * Analyzes whether this route should execute, allowing the to separate the check from the execution.
   *
   * @param session an {@link ExpressionManagerSession}
   * @return {@code true} if this route should execute
   */
  boolean shouldExecute(ExpressionManagerSession session) {
    return route.accepts(session);
  }

  /**
   * Routes the incoming event through the route's sink.
   *
   * @param event the {@link CoreEvent} to process
   */
  void execute(CoreEvent event) {
    sinkRecorder.next(event);
  }

  /**
   * Signals the given throwable on the route's sink.
   *
   * @param throwable The {@link Throwable} to signal.
   */
  void error(Throwable throwable) {
    sinkRecorder.error(throwable);
  }

  /**
   * Triggers the underlying {@link Flux} completion signal.
   */
  public void complete() {
    sinkRecorder.complete();
  }

  public Flux<CoreEvent> getPublisher() {
    return publisher;
  }

  public Processor getProcessor() {
    return route.getProcessor();
  }

}
