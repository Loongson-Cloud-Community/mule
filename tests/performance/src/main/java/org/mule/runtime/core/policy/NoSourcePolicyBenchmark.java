/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.policy;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.mule.runtime.core.api.event.EventContextFactory.create;
import static org.mule.runtime.core.internal.execution.SourcePolicyTestUtils.block;

import org.mule.AbstractBenchmark;
import org.mule.runtime.api.functional.Either;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.util.func.CheckedFunction;
import org.mule.runtime.core.internal.policy.MessageSourceResponseParametersProcessor;
import org.mule.runtime.core.internal.policy.NoSourcePolicy;
import org.mule.runtime.core.internal.policy.SourcePolicy;
import org.mule.runtime.core.internal.policy.SourcePolicyFailureResult;
import org.mule.runtime.core.internal.policy.SourcePolicySuccessResult;

import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Threads;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(MICROSECONDS)
public class NoSourcePolicyBenchmark extends AbstractBenchmark {

  private SourcePolicy handler;
  private MessageSourceResponseParametersProcessor sourceRpp;

  @Setup(Level.Trial)
  public void setUp() {
    handler = new NoSourcePolicy(eventPub -> Flux.from(eventPub)
        .flatMap(e -> Mono.just(e)));

    sourceRpp = new MessageSourceResponseParametersProcessor() {

      @Override
      public CheckedFunction<CoreEvent, Map<String, Object>> getSuccessfulExecutionResponseParametersFunction() {
        return event -> emptyMap();
      }

      @Override
      public CheckedFunction<CoreEvent, Map<String, Object>> getFailedExecutionResponseParametersFunction() {
        return event -> emptyMap();
      }
    };
  }

  @Benchmark
  @Threads(Threads.MAX)
  public Either<SourcePolicyFailureResult, SourcePolicySuccessResult> source() throws Throwable {
    CoreEvent event;
    Message.Builder messageBuilder = Message.builder().value(PAYLOAD);
    CoreEvent.Builder eventBuilder =
        CoreEvent.builder(create("", "", CONNECTOR_LOCATION, null, empty())).message(messageBuilder.build());
    event = eventBuilder.build();

    return block(callback -> handler.process(event, sourceRpp, callback));
  }

}
