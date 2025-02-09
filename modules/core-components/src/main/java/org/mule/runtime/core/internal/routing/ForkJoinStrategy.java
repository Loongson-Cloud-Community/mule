/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.routing;

import static java.util.Objects.requireNonNull;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.privileged.processor.chain.MessageProcessorChain;

import org.reactivestreams.Publisher;

/**
 * Strategy that defines how a set of {@link RoutingPair}'s each consisting of a {@link Processor} and {@link CoreEvent} will be
 * processed and a single result {@link CoreEvent} returned. This will normally be used for:
 * <ul>
 * <li>Routing {@code n} {@link CoreEvent}'s to the same {@link MessageProcessorChain}.
 * <li>Routing a single {@link CoreEvent} to {@code n} {@link MessageProcessorChain}'s.
 * </ul>
 * <p>
 * Implementations will typically implement parallel behavior where the invocation of each route is independent and then results
 * are aggregated, but other implements such as strict sequential invocation or even the use of a shared context between
 * invocations are possible.
 * <p>
 * While the result of this strategy is a single {@link CoreEvent} implementations are free to decide if the event should be
 * emitted only once all results are available, or if it emits the event immediately and then makes the results available via an
 * {@link java.util.Iterator} or {@code Publisher<InternalEvent>} payload. Implementations may also return the original
 * {@link CoreEvent} therefore performing a simple join with no aggregation.
 *
 * @since 4.0
 */
public interface ForkJoinStrategy {

  /**
   * Processes {@code n} routing pairs and aggregates the result as defined in the strategy implementation.
   *
   * @param original     the original event
   * @param routingPairs the routing pairs to be processed
   * @return the aggregated result of processing the routing pairs
   */
  Publisher<CoreEvent> forkJoin(CoreEvent original, Publisher<RoutingPair> routingPairs);

  /**
   * Define the tuple of {@link MessageProcessorChain} and {@link CoreEvent} used for a
   * {@link org.mule.runtime.core.internal.routing.AbstractForkJoinRouter} to define the parts/routes to be processed and used by
   * implementations of {@link ForkJoinStrategy} to implement specific logic around how these are processed and aggregated.
   */
  final class RoutingPair {

    private final MessageProcessorChain route;
    private final CoreEvent event;

    public static RoutingPair of(CoreEvent event, MessageProcessorChain route) {
      return new RoutingPair(event, route);
    }

    private RoutingPair(CoreEvent event, MessageProcessorChain route) {

      this.event = requireNonNull(event);
      this.route = requireNonNull(route);
    }

    public MessageProcessorChain getRoute() {
      return route;
    }

    public CoreEvent getEvent() {
      return event;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      RoutingPair pair = (RoutingPair) o;

      if (!route.equals(pair.route)) {
        return false;
      }
      return event.equals(pair.event);
    }

    @Override
    public int hashCode() {
      int result = route.hashCode();
      result = 31 * result + event.hashCode();
      return result;
    }
  }

}
