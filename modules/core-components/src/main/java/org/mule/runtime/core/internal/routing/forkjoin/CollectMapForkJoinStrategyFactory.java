/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.routing.forkjoin;

import static java.util.stream.Collectors.toMap;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.api.metadata.DataType.MULE_MESSAGE_MAP;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.routing.ForkJoinStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * {@link ForkJoinStrategy} that:
 * <ul>
 * <li>Performs parallel execution of route pairs subject to {@code maxConcurrency}.
 * <li>Merges variables using a last-wins strategy.
 * <li>Waits for the completion of all routes before emitting a result event, with an optional timeout.
 * <li>Collects results into a result {@link CoreEvent} with a {@link java.util.Map} collection that contains {@link String} as
 * key and {@link org.mule.runtime.api.message.Message} as value in the payload where the {@link java.util.Map} key is a string
 * representation of the sequence number of the
 * {@link ForkJoinStrategy#org.mule.runtime.core.internal.routing.ForkJoinStrategy.RoutingPair}.
 * <li>Will processor all routes, regardless of errors, and propagating a composite exception where there were one or more errors.
 * </ul>
 */
public class CollectMapForkJoinStrategyFactory extends AbstractForkJoinStrategyFactory {

  @Override
  protected Function<List<CoreEvent>, CoreEvent> createResultEvent(CoreEvent original,
                                                                   CoreEvent.Builder resultBuilder) {
    return list -> resultBuilder
        .message(of(list.stream().collect(toMap(event -> Integer.toString(event.getGroupCorrelation().get().getSequence()),
                                                event -> event.getMessage(),
                                                (a, b) -> b,
                                                LinkedHashMap::new))))
        .build();
  }

  @Override
  public DataType getResultDataType() {
    return MULE_MESSAGE_MAP;
  }

}
