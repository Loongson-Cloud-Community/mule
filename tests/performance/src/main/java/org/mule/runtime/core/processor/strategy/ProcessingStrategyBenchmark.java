/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.processor.strategy;

import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static reactor.core.publisher.FluxSink.OverflowStrategy.ERROR;

import org.mule.AbstractBenchmark;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.ReactiveProcessor;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.internal.processor.strategy.DirectProcessingStrategyFactory;
import org.mule.runtime.core.internal.processor.strategy.TransactionAwareStreamEmitterProcessingStrategyFactory;

import java.util.function.Function;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class ProcessingStrategyBenchmark extends AbstractBenchmark {

  private MuleContext muleContext;

  private ProcessingStrategy directPs;
  private ProcessingStrategy emitterPs;
  private Flow flow;

  private Sink directSink;
  private Sink emitterSink;

  private FluxSink<CoreEvent> directPipeline;
  private FluxSink<CoreEvent> emitterPipeline;

  private FluxSink<CoreEvent> directProcessor;
  private FluxSink<CoreEvent> emitterProcessor;

  private Sink directAllSink;
  private Sink emitterAllSink;

  @Setup(Level.Trial)
  public void setUp() throws MuleException {
    muleContext = createMuleContextWithServices();

    directPs = new DirectProcessingStrategyFactory().create(muleContext, "direct_mb");
    startIfNeeded(directPs);
    emitterPs = new TransactionAwareStreamEmitterProcessingStrategyFactory().create(muleContext, "emitter_mb");
    startIfNeeded(emitterPs);

    flow = createFlow(muleContext);

    final ReactiveProcessor processor = p -> Flux.from(p).doOnNext(e -> {
      Blackhole.consumeCPU(100);
    });

    directSink = directPs.createSink(flow, publisher -> baseFlux(publisher, processor));
    emitterSink = emitterPs.createSink(flow, publisher -> baseFlux(publisher, processor));

    Flux.<CoreEvent>create(s -> directPipeline = s, ERROR)
        .transform(directPs.onPipeline(publisher -> baseFlux(publisher, processor)))
        .subscribe();
    Flux.<CoreEvent>create(s -> emitterPipeline = s, ERROR)
        .transform(emitterPs.onPipeline(publisher -> baseFlux(publisher, processor)))
        .subscribe();
    Flux.<CoreEvent>create(s -> directProcessor = s, ERROR)
        .transform(directPs.onProcessor(publisher -> baseFlux(publisher, processor)))
        .subscribe();
    Flux.<CoreEvent>create(s -> emitterProcessor = s, ERROR)
        .transform(emitterPs.onProcessor(publisher -> baseFlux(publisher, processor)))
        .subscribe();

    directAllSink =
        directPs.createSink(flow, publisher -> baseFlux(publisher, directPs.onPipeline(directPs.onProcessor(processor))));
    emitterAllSink =
        emitterPs.createSink(flow, publisher -> baseFlux(publisher, emitterPs.onPipeline(emitterPs.onProcessor(processor))));
  }

  private Flux<CoreEvent> baseFlux(Publisher<CoreEvent> publisher,
                                   Function<? super Flux<CoreEvent>, ? extends Publisher<CoreEvent>> transformFunction) {
    return Flux.from(publisher)
        .transform(transformFunction)
        .doOnNext(event -> ((MonoSink<CoreEvent>) (event.getMessage().getPayload().getValue())).success(event))
        .onErrorContinue((t, event) -> {
          ((MonoSink<CoreEvent>) (((CoreEvent) event).getMessage().getPayload().getValue())).error(t);
        });
  }

  @Benchmark
  @Threads(Threads.MAX)
  public CoreEvent directSink() {
    return Mono.<CoreEvent>create(resultSink -> directSink.accept(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(Threads.MAX)
  public CoreEvent emitterSink() {
    return Mono.<CoreEvent>create(resultSink -> emitterSink.accept(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(1)
  public CoreEvent directPipeline() {
    return Mono.<CoreEvent>create(resultSink -> directPipeline.next(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(1)
  public CoreEvent emitterPipeline() {
    return Mono.<CoreEvent>create(resultSink -> emitterPipeline.next(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(1)
  public CoreEvent directProcessor() {
    return Mono.<CoreEvent>create(resultSink -> directProcessor.next(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(1)
  public CoreEvent emitterProcessor() {
    return Mono.<CoreEvent>create(resultSink -> emitterProcessor.next(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(Threads.MAX)
  public CoreEvent directAllSink() {
    return Mono.<CoreEvent>create(resultSink -> directAllSink.accept(createEvent(flow, resultSink))).block();
  }

  @Benchmark
  @Threads(Threads.MAX)
  public CoreEvent emitterAllSink() {
    return Mono.<CoreEvent>create(resultSink -> emitterAllSink.accept(createEvent(flow, resultSink))).block();
  }
}
