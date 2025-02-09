/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.processor.strategy;

import static org.mule.runtime.core.api.config.MuleProperties.MULE_PROFILING_SERVICE_KEY;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.BLOCKING;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE;
import static org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy.DROP;
import static org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy.FAIL;
import static org.mule.runtime.core.api.source.MessageSource.BackPressureStrategy.WAIT;
import static org.mule.runtime.core.api.transaction.TransactionCoordination.getInstance;
import static org.mule.runtime.core.internal.processor.strategy.AbstractProcessingStrategy.TRANSACTIONAL_ERROR_MESSAGE;
import static org.mule.runtime.core.internal.processor.strategy.AbstractProcessingStrategyTestCase.Mode.SOURCE;
import static org.mule.runtime.core.internal.processor.strategy.AbstractProcessingStrategyTestCase.RejectingScheduler.REJECTION_COUNT;
import static org.mule.runtime.core.internal.processor.strategy.AbstractStreamProcessingStrategyFactory.CORES;
import static org.mule.runtime.core.internal.processor.strategy.AbstractStreamProcessingStrategyFactory.DEFAULT_BUFFER_SIZE;
import static org.mule.runtime.core.internal.profiling.NoopCoreEventTracer.getNoopCoreEventTracer;
import static org.mule.tck.probe.PollingProber.probe;
import static org.mule.tck.util.MuleContextUtils.getNotificationDispatcher;
import static org.mule.test.allure.AllureConstants.ExecutionEngineFeature.ExecutionEngineStory.BACKPRESSURE;
import static org.mule.test.allure.AllureConstants.ProcessingStrategiesFeature.PROCESSING_STRATEGIES;
import static org.mule.test.allure.AllureConstants.ProcessingStrategiesFeature.ProcessingStrategiesStory.PROACTOR;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assume.assumeThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Flux.from;
import static reactor.core.scheduler.Schedulers.fromExecutorService;
import static reactor.util.concurrent.Queues.XS_BUFFER_SIZE;

import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;
import org.mule.runtime.api.profiling.ProfilingService;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.ReactiveProcessor;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.AsyncProcessingStrategyFactory;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.internal.construct.FlowBackPressureMaxConcurrencyExceededException;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.processor.strategy.ProactorStreamEmitterProcessingStrategyFactory.ProactorStreamEmitterProcessingStrategy;
import org.mule.runtime.core.internal.profiling.DefaultProfilingService;
import org.mule.runtime.core.internal.rx.FluxSinkRecorder;
import org.mule.runtime.tracer.api.EventTracer;
import org.mule.tck.TriggerableMessageSource;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.testmodels.mule.TestTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.InOrder;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(PROCESSING_STRATEGIES)
@Story(PROACTOR)
public class ProactorStreamEmitterProcessingStrategyTestCase extends AbstractProcessingStrategyTestCase {

  private static final Logger LOGGER = getLogger(ProactorStreamEmitterProcessingStrategyTestCase.class);

  private final ProfilingService profilingService = new DefaultProfilingService() {

    @Override
    public ProfilingDataConsumerDiscoveryStrategy getDiscoveryStrategy() {
      return () -> singleton(profilingDataConsumer);
    }

    @Override
    public EventTracer<CoreEvent> getCoreEventTracer() {
      return getNoopCoreEventTracer();
    }

  };

  @Rule
  public ExpectedException expectedException = none();

  public ProactorStreamEmitterProcessingStrategyTestCase(Mode mode, boolean profiling) {
    super(mode, profiling);
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    Map<String, Object> startupRegistryObjects = new HashMap<>();
    startupRegistryObjects.put(MULE_PROFILING_SERVICE_KEY, profilingService);
    return startupRegistryObjects;
  }

  @Override
  protected ProcessingStrategy createProcessingStrategy(MuleContext muleContext, String schedulersNamePrefix) {
    return createProcessingStrategy(muleContext, schedulersNamePrefix, MAX_VALUE);
  }

  protected ProcessingStrategy createProcessingStrategy(MuleContext muleContext, String schedulersNamePrefix,
                                                        int maxConcurrency) {
    return new ProactorStreamEmitterProcessingStrategy(XS_BUFFER_SIZE,
                                                       2,
                                                       () -> cpuLight,
                                                       () -> cpuLight,
                                                       () -> blocking,
                                                       () -> cpuIntensive,
                                                       CORES,
                                                       maxConcurrency,
                                                       true,
                                                       () -> muleContext.getConfiguration().getShutdownTimeout());
  }

  @Override
  @Description("With the ProactorProcessingStrategy, when all processor are CPU_LIGHT then they are all exectured in a single "
      + " cpu light thread.")
  public void singleCpuLight() throws Exception {
    super.singleCpuLight();
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("When ProactorProcessingStrategy is configured, two concurrent requests may be processed by two different "
      + " cpu light threads. MULE-11132 is needed for true reactor behaviour.")
  public void singleCpuLightConcurrent() throws Exception {
    super.singleCpuLightConcurrent();
    assertThat(threads, hasSize(between(1, 2)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), between(1l, 2l));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, when all processor are CPU_LIGHT then they are all exectured in a single "
      + " cpu light thread.")
  public void multipleCpuLight() throws Exception {
    super.multipleCpuLight();
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, a BLOCKING message processor is scheduled on a IO thread.")
  public void singleBlocking() throws Exception {
    super.singleBlocking();
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, a BLOCKING message processor is scheduled on a IO thread.")
  public void singleBlockingInnerPublisher() throws Exception {
    super.singleBlockingInnerPublisher();
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, each BLOCKING message processor is scheduled on a IO thread. These may, or "
      + "may not, be the same thread.")
  public void multipleBlocking() throws Exception {
    super.multipleBlocking();
    assertThat(threads, hasSize(between(1, 3)));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), between(1l, 3l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, a CPU_INTENSIVE message processor is scheduled on a CPU intensive thread.")
  public void singleCpuIntensive() throws Exception {
    super.singleCpuIntensive();
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_INTENSIVE)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, each CPU_INTENSIVE message processor is scheduled on a CPU Intensive thread."
      + " These may, or may not, be the same thread.")
  public void multipleCpuIntensive() throws Exception {
    super.multipleCpuIntensive();
    assertThat(threads, hasSize(between(1, 3)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_INTENSIVE)).count(), between(1l, 3l));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, when there is a mix of processor processing types, each processor is "
      + "scheduled on the correct scheduler.")
  public void mix() throws Exception {
    super.mix();
    assertThat(threads, hasSize(equalTo(3)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_INTENSIVE)).count(), equalTo(1l));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("With the ProactorProcessingStrategy, when there is a mix of processor processing types, each processor is "
      + "scheduled on the correct scheduler.")
  public void mix2() throws Exception {
    super.mix2();
    assertThat(threads, hasSize(between(3, 7)));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_INTENSIVE)).count(), between(1l, 2l));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), between(1l, 2l));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), between(1l, 3l));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("When the ProactorProcessingStrategy is configured and a transaction is active processing fails with an error")
  public void tx() throws Exception {
    flow = flowBuilder.get().processors(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor).build();
    startFlow();

    getInstance().bindTransaction(new TestTransaction("appName", getNotificationDispatcher(muleContext)));

    expectedException.expect(MessagingException.class);
    expectedException.expectCause(instanceOf(DefaultMuleException.class));
    expectedException.expectCause(hasMessage(equalTo(TRANSACTIONAL_ERROR_MESSAGE)));
    processFlow(testEvent());
  }

  @Override
  @Description("When the ReactorProcessingStrategy is configured and a transaction is active processing fails with an error")
  public void asyncCpuLight() throws Exception {
    super.asyncCpuLight();
    assertThat(threads.toString(), threads, hasSize(between(1, 2)));
    assertThat(threads.toString(), threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), between(1l, 2l));
    assertThat(threads.toString(), threads, not(hasItem(startsWith(IO))));
    assertThat(threads.toString(), threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads.toString(), threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Override
  @Description("Concurrent stream with concurrency of 8 only uses two CPU_LIGHT threads.")
  public void concurrentStream() throws Exception {
    super.concurrentStream();
    assertThat(threads.toString(), threads, hasSize(2));
    assertThat(threads.toString(), threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), equalTo(2l));
  }

  @Test
  @Description("If IO pool is busy OVERLOAD error is thrown")
  public void blockingRejectedExecution() throws Exception {
    Scheduler blockingSchedulerSpy = spy(blocking);
    RejectingScheduler rejectingSchedulerSpy = new RejectingScheduler(blockingSchedulerSpy);

    flow = flowBuilder.get().processors(blockingProcessor)
        .processingStrategyFactory((context, prefix) -> new ProactorStreamEmitterProcessingStrategy(DEFAULT_BUFFER_SIZE,
                                                                                                    1,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> rejectingSchedulerSpy,
                                                                                                    () -> cpuIntensive,
                                                                                                    1,
                                                                                                    2,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout()))
        .build();
    startFlow();
    rejectingSchedulerSpy.reset();

    processFlow(testEvent());

    // Reactor dispatches different tasks to the scheduler for processing the task, so we cannot assume a 1-1 ratio between events
    // and calls to the scheduler, or that they happen all in a predictable order (threading, ya know...).
    probe(() -> {
      verify(blockingSchedulerSpy, times(1)).submit(any(Callable.class));
      assertThat(rejectingSchedulerSpy.getRejections(), is(greaterThanOrEqualTo(REJECTION_COUNT)));
      return true;
    });

    assertThat(threads, hasSize(1));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("If CPU INTENSIVE pool is busy OVERLOAD error is thrown")
  public void cpuIntensiveRejectedExecution() throws Exception {
    Scheduler cpuIntensiveSchedulerSpy = spy(cpuIntensive);
    RejectingScheduler rejectingSchedulerSpy = new RejectingScheduler(cpuIntensiveSchedulerSpy);

    flow = flowBuilder.get().processors(cpuIntensiveProcessor)
        .processingStrategyFactory((context, prefix) -> new ProactorStreamEmitterProcessingStrategy(DEFAULT_BUFFER_SIZE,
                                                                                                    1,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> blocking,
                                                                                                    () -> rejectingSchedulerSpy,
                                                                                                    1,
                                                                                                    2,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout()))
        .build();
    startFlow();
    rejectingSchedulerSpy.reset();

    processFlow(testEvent());

    // Reactor dispatches different tasks to the scheduler for processing the task, so we cannot assume a 1-1 ratio between events
    // and calls to the scheduler, or that they happen all in a predictable order (threading, ya know...).
    probe(() -> {
      verify(cpuIntensiveSchedulerSpy, times(1)).submit(any(Callable.class));
      assertThat(rejectingSchedulerSpy.getRejections(), is(greaterThanOrEqualTo(REJECTION_COUNT)));
      return true;
    });

    assertThat(threads, hasSize(1));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_INTENSIVE)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("If max concurrency is 1, only 1 thread is used for CPU_LITE processors and further requests blocks. When " +
      "maxConcurrency < subscribers processing is done on ring-buffer thread.")
  public void singleCpuLightConcurrentMaxConcurrency1() throws Exception {
    internalConcurrent(flowBuilder.get()
        .processingStrategyFactory(
                                   (context, prefix) -> new ProactorStreamEmitterProcessingStrategy(DEFAULT_BUFFER_SIZE,
                                                                                                    1,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> blocking,
                                                                                                    () -> cpuIntensive,
                                                                                                    CORES,
                                                                                                    1,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout())),
                       true, CPU_LITE, 1);
    assertThat(threads, hasSize(1));
    assertThat(threads, hasItem(startsWith(CPU_LIGHT)));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("If max concurrency is 2, only 2 threads are used for CPU_LITE processors and further requests blocks.")
  public void singleCpuLightConcurrentMaxConcurrency2() throws Exception {
    internalConcurrent(flowBuilder.get()
        .processingStrategyFactory(
                                   (context, prefix) -> new ProactorStreamEmitterProcessingStrategy(DEFAULT_BUFFER_SIZE,
                                                                                                    2,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> blocking,
                                                                                                    () -> cpuIntensive,
                                                                                                    CORES,
                                                                                                    2,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout())),
                       true, CPU_LITE, 2);
    assertThat(threads, hasSize(2));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("If max concurrency is 1, only 1 thread is used for BLOCKING processors and further requests blocks. When " +
      "maxConcurrency < subscribers processing is done on ring-buffer thread.")
  public void singleBlockingConcurrentMaxConcurrency1() throws Exception {
    assumeThat(mode, is(SOURCE));

    internalConcurrent(flowBuilder.get()
        .processingStrategyFactory(
                                   (context, prefix) -> new ProactorStreamEmitterProcessingStrategy(DEFAULT_BUFFER_SIZE,
                                                                                                    2,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> blocking,
                                                                                                    () -> cpuIntensive,
                                                                                                    CORES,
                                                                                                    1,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout())),
                       true, BLOCKING, 1);
    assertThat(threads, hasSize(1));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("If max concurrency is 2, only 2 threads are used for BLOCKING processors and further requests blocks.")
  public void singleBlockingConcurrentMaxConcurrency2() throws Exception {
    assumeThat(mode, is(SOURCE));

    internalConcurrent(flowBuilder.get()
        .processingStrategyFactory(
                                   (context, prefix) -> new ProactorStreamEmitterProcessingStrategy(DEFAULT_BUFFER_SIZE,
                                                                                                    2,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> blocking,
                                                                                                    () -> cpuIntensive,
                                                                                                    1,
                                                                                                    2,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout())),
                       true, BLOCKING, 2);
    assertThat(threads, hasSize(2));
    // assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(2l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("Notifications are invoked on CPU_LITE thread")
  public void asyncProcessorNotificationExecutionThreads() throws Exception {
    AtomicReference<Thread> beforeThread = new AtomicReference<>();
    AtomicReference<Thread> afterThread = new AtomicReference<>();
    testAsyncCpuLightNotificationThreads(beforeThread, afterThread);
    assertThat(beforeThread.get().getName(), startsWith(CPU_LIGHT));
    assertThat(afterThread.get().getName(), startsWith(CPU_LIGHT));
  }

  @Test
  @Story(BACKPRESSURE)
  @Description("When back-pressure strategy is 'WAIT' the source thread blocks and all requests are processed.")
  public void sourceBackPressureWait() throws Exception {
    testBackPressure(WAIT, equalTo(STREAM_ITERATIONS), equalTo(0), equalTo(STREAM_ITERATIONS));
  }

  @Test
  @Story(BACKPRESSURE)
  @Description("When back-pressure strategy is 'FAIL' some requests fail with an OVERLOAD error.")
  public void sourceBackPressureFail() throws Exception {
    testBackPressure(FAIL, lessThanOrEqualTo(STREAM_ITERATIONS), greaterThan(0), equalTo(STREAM_ITERATIONS));
  }

  @Test
  @Story(BACKPRESSURE)
  @Description("When back-pressure strategy is 'DROP' the flow rejects requests in the same way way with 'FAIL. It is the source that handles FAIL and DROP differently.")
  public void sourceBackPressureDrop() throws Exception {
    testBackPressure(DROP, lessThanOrEqualTo(STREAM_ITERATIONS), greaterThan(0), equalTo(STREAM_ITERATIONS));
  }

  @Test
  @Description("When concurrency < parallelism IO threads are still used for blocking processors to avoid cpuLight thread starvation.")
  public void concurrencyLessThanParallelism() throws Exception {
    flow = flowBuilder.get()
        .processingStrategyFactory((context, prefix) -> new ProactorStreamEmitterProcessingStrategy(XS_BUFFER_SIZE,
                                                                                                    1,
                                                                                                    () -> cpuLight,
                                                                                                    () -> cpuLight,
                                                                                                    () -> blocking,
                                                                                                    () -> cpuIntensive,
                                                                                                    4,
                                                                                                    2,
                                                                                                    true,
                                                                                                    () -> muleContext
                                                                                                        .getConfiguration()
                                                                                                        .getShutdownTimeout()))
        .processors(blockingProcessor)
        .build();
    startFlow();

    processFlow(testEvent());
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  @Description("If the processing type is IO_RW then processing occurs in BLOCKING thread.")
  public void singleIOWRW() throws Exception {
    super.singleIORW(() -> testEvent(), contains(IO));
    assertThat(threads, hasSize(equalTo(1)));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo(1l));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

  @Test
  public void backpressureOnInnerCpuIntensiveSchedulerBusy() throws Exception {
    assumeThat(mode, is(SOURCE));

    MultipleInvocationLatchedProcessor latchedProcessor =
        new MultipleInvocationLatchedProcessor(ReactiveProcessor.ProcessingType.CPU_INTENSIVE, 4);

    triggerableMessageSource = new TriggerableMessageSource(FAIL);

    flow = flowBuilder.get()
        .processingStrategyFactory(new AsyncProcessingStrategyFactory() {

          private int maxConcurrency = MAX_VALUE;

          @Override
          public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
            return createProcessingStrategy(muleContext, schedulersNamePrefix, maxConcurrency);
          }

          @Override
          public void setMaxConcurrencyEagerCheck(boolean maxConcurrencyEagerCheck) {
            // Nothing to do
          }

          @Override
          public void setMaxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
          }
        })
        .source(triggerableMessageSource)
        .processors(cpuLightProcessor, latchedProcessor)
        .maxConcurrency(6)
        .build();
    startFlow();

    List<Future> futures = new ArrayList<>();

    try {
      // Fill the threads, the queue and an extra one to keep retrying
      for (int i = 0; i < (2 * 2) + 1; ++i) {
        futures.add(asyncExecutor.submit(() -> processFlow(newEvent())));
      }

      new PollingProber(10000, 10)
          .check(new JUnitLambdaProbe(() -> cpuIntensive.executor.toString().contains("queued tasks = 2")));

      // Give time for the extra dispatch to get to the point where it starts retrying
      Thread.sleep(1000);

      expectedException
          .expectMessage("Flow \"flow\" is unable to accept new events at this time. Reason: REQUIRED_SCHEDULER_BUSY");
      processFlow(newEvent());
    } finally {
      latchedProcessor.release();
      latchedProcessor.getAllLatchedLatch().await();

      futures.forEach(f -> {
        try {
          f.get(RECEIVE_TIMEOUT, MILLISECONDS);
        } catch (Exception e) {
          // It is possible for the expected exception to be thrown by a future if the processFlow
          // in the main thread enters in the time window where it can be retried.
          throw new MuleRuntimeException(e);
        }
      });
    }
  }

  @Test
  public void backpressureOnInnerCpuIntensiveSchedulerBusyRecovery() throws Exception {
    assumeThat(mode, is(SOURCE));

    MultipleInvocationLatchedProcessor latchedProcessor =
        new MultipleInvocationLatchedProcessor(ReactiveProcessor.ProcessingType.CPU_INTENSIVE, 5);

    triggerableMessageSource = new TriggerableMessageSource(FAIL);

    flow = flowBuilder.get()
        .source(triggerableMessageSource)
        .processors(cpuLightProcessor, latchedProcessor).build();
    startFlow();

    List<Future> futures = new ArrayList<>();

    try {
      // Fill the threads, the queue and an extra one to keep retrying
      for (int i = 0; i < (2 * 2) + 1; ++i) {
        futures.add(asyncExecutor.submit(() -> processFlow(newEvent())));
      }

      // Give time for the extra dispatch to get to the point where it starts retrying
      Thread.sleep(500);

      latchedProcessor.release();

      Thread.sleep(500);
      processFlow(newEvent());
    } finally {
      futures.forEach(f -> {
        try {
          f.get(RECEIVE_TIMEOUT, MILLISECONDS);
        } catch (Exception e) {
          throw new MuleRuntimeException(e);
        }
      });
    }
  }

  @Test
  @Story(BACKPRESSURE)
  public void eagerBackpressureOnMaxConcurrencyHit() throws Exception {
    assumeThat(mode, is(SOURCE));

    MultipleInvocationLatchedProcessor latchedProcessor =
        new MultipleInvocationLatchedProcessor(CPU_LITE, 1);

    triggerableMessageSource = new TriggerableMessageSource(FAIL);

    flow = flowBuilder.get()
        .source(triggerableMessageSource)
        .processors(latchedProcessor)
        .processingStrategyFactory((muleContext, prefix) -> createProcessingStrategy(muleContext, prefix, 1))
        .build();

    startFlow();

    List<Future> futures = new ArrayList<>();

    try {
      futures.add(asyncExecutor.submit(() -> processFlow(newEvent())));

      // Give time for the dispatch to get to the capacity check
      Thread.sleep(500);

      expectedException.expectCause(instanceOf(FlowBackPressureMaxConcurrencyExceededException.class));
      processFlow(newEvent());
    } finally {
      latchedProcessor.release();
      latchedProcessor.getAllLatchedLatch().await();

      futures.forEach(f -> {
        try {
          f.get(RECEIVE_TIMEOUT, MILLISECONDS);
        } catch (Exception e) {
          throw new MuleRuntimeException(e);
        }
      });
    }
  }

  @Test
  public void eagerBackpressureOnMaxConcurrencyHitRecovery() throws Exception {
    assumeThat(mode, is(SOURCE));

    MultipleInvocationLatchedProcessor latchedProcessor =
        new MultipleInvocationLatchedProcessor(CPU_LITE, 1);

    triggerableMessageSource = new TriggerableMessageSource(FAIL);

    flow = flowBuilder.get()
        .source(triggerableMessageSource)
        .processors(latchedProcessor)
        .processingStrategyFactory((muleContext, prefix) -> createProcessingStrategy(muleContext, prefix, 1))
        .build();

    startFlow();

    List<Future> futures = new ArrayList<>();

    try {
      futures.add(asyncExecutor.submit(() -> processFlow(newEvent())));

      // Give time for the dispatch to get to the capacity check
      Thread.sleep(500);
      latchedProcessor.release();
      Thread.sleep(500);

      // expectedException.expectCause(instanceOf(FlowBackPressureException.class));
      processFlow(newEvent());
    } finally {
      latchedProcessor.getAllLatchedLatch().await();

      futures.forEach(f -> {
        try {
          f.get(RECEIVE_TIMEOUT, MILLISECONDS);
        } catch (Exception e) {
          throw new MuleRuntimeException(e);
        }
      });
    }
  }

  @Test
  @Issue("MULE-17048")
  @Description("Verify that the event loop scheduler (cpu lite) is stopped before the others. Otherwise, an interrupted event may resume processing on ")
  public void schedulersStoppedInOrder() throws MuleException {
    spySchedulers();

    flow = flowBuilder.get().processors(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor).build();
    startFlow();

    final ProcessingStrategy ps = createProcessingStrategy(muleContext, "schedulersStoppedInOrder");

    initialiseIfNeeded(ps, muleContext);
    startIfNeeded(ps);

    final Sink sink = ps.createSink(flow, flow);
    disposeIfNeeded(sink, LOGGER);

    stopIfNeeded(ps);
    disposeIfNeeded(ps, LOGGER);

    final InOrder inOrder = inOrder(cpuLight, cpuIntensive, blocking);
    inOrder.verify(cpuLight).stop();
    inOrder.verify(blocking).stop();
    inOrder.verify(cpuIntensive).stop();
  }

  @Test
  public void schedulersStoppedOnFluxesComplete() throws MuleException {
    spySchedulers();

    flow = flowBuilder.get().processors(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor).build();
    startFlow();

    final ProcessingStrategy ps = createProcessingStrategy(muleContext, "schedulersStoppedInOrder");

    initialiseIfNeeded(ps, muleContext);
    startIfNeeded(ps);

    final Sink sink = ps.createSink(flow, flow);
    disposeIfNeeded(sink, LOGGER);

    final InOrder inOrder = inOrder(cpuLight, cpuIntensive, blocking);
    inOrder.verify(cpuLight).stop();
    inOrder.verify(blocking).stop();
    inOrder.verify(cpuIntensive).stop();
  }

  @Test
  public void schedulersStoppedOnInternalFluxesComplete() throws MuleException {
    spySchedulers();

    flow = flowBuilder.get().processors(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor).build();
    startFlow();

    final ProcessingStrategy ps = createProcessingStrategy(muleContext, "schedulersStoppedInOrder");

    initialiseIfNeeded(ps, muleContext);
    startIfNeeded(ps);

    final Sink sink = ps.createSink(flow, flow);

    final FluxSinkRecorder<CoreEvent> internalSink = new FluxSinkRecorder<>();
    ps.registerInternalSink(internalSink.flux(), "internalSink");

    disposeIfNeeded(sink, LOGGER);
    verify(cpuLight, never()).stop();
    verify(blocking, never()).stop();
    verify(cpuIntensive, never()).stop();

    internalSink.complete();
    final InOrder inOrder = inOrder(cpuLight, cpuIntensive, blocking);
    inOrder.verify(cpuLight).stop();
    inOrder.verify(blocking).stop();
    inOrder.verify(cpuIntensive).stop();
  }

  @Test
  @Issue("MULE-18183")
  public void disposeWithRegisteredInternalSink() throws MuleException {
    spySchedulers();

    final ProcessingStrategy ps = createProcessingStrategy(muleContext, "withRegisteredInternalSink");

    initialiseIfNeeded(ps, muleContext);
    startIfNeeded(ps);

    final FluxSinkRecorder<CoreEvent> fluxSinkRecorder = new FluxSinkRecorder<>();
    ps.registerInternalSink(fluxSinkRecorder.flux(), "justAnEvent");

    fluxSinkRecorder.complete();
    disposeIfNeeded(ps, LOGGER);

    verify(cpuLight).stop();
    verify(blocking).stop();
    verify(cpuIntensive).stop();

  }

  @Test
  @Issue("MULE-18884")
  @Description("Check that all internal sinks are complete when ps.dispose() returns.")
  public void disposeWaitsForRegisteredInternalSinkCompletion() throws MuleException, InterruptedException {
    final ProcessingStrategy ps = createProcessingStrategy(muleContext, "withRegisteredInternalSink");

    initialiseIfNeeded(ps, muleContext);
    startIfNeeded(ps);

    final Latch completionLatch = new Latch();

    final FluxSinkRecorder<CoreEvent> fluxSinkRecorder = new FluxSinkRecorder<>();
    ps.registerInternalSink(fluxSinkRecorder.flux()
        // force completion to run asynchronously to generate the situation that leads to the bug
        .publishOn(fromExecutorService(blocking))
        .doOnComplete(() -> {
          try {
            // dispose timeout
            // + latch await timeout
            // + some buffer
            sleep(muleContext.getConfiguration().getShutdownTimeout() + 2000);
          } catch (InterruptedException e) {
            currentThread().interrupt();
            return;
          }

          completionLatch.countDown();
        }), "justAnEvent");
    fluxSinkRecorder.complete();

    disposeIfNeeded(ps, LOGGER);
    assertThat("ps dispose should have waited for the internal sink completion",
               completionLatch.await(1000, MILLISECONDS),
               is(true));
  }

  @Test
  @Issue("MULE-18183")
  public void disposeWithConfiguredInternalPublisher() throws MuleException {
    spySchedulers();

    final ProcessingStrategy ps = createProcessingStrategy(muleContext, "withConfiguredInternalPublisher");

    initialiseIfNeeded(ps, muleContext);
    startIfNeeded(ps);

    final FluxSinkRecorder<CoreEvent> fluxSinkRecorder = new FluxSinkRecorder<>();
    from(ps.configureInternalPublisher(fluxSinkRecorder.flux()))
        .subscribe();

    fluxSinkRecorder.complete();
    disposeIfNeeded(ps, LOGGER);

    verify(cpuLight).stop();
    verify(blocking).stop();
    verify(cpuIntensive).stop();
  }

  @Test
  public void testProfiling() throws Exception {
    muleContext.start();
    triggerableMessageSource = new TriggerableMessageSource(FAIL);
    flow = flowBuilder.get()
        .source(triggerableMessageSource)
        .processors(cpuLightProcessor)
        .processingStrategyFactory((muleContext, prefix) -> createProcessingStrategy(muleContext, prefix, MAX_VALUE))
        .build();
    startFlow();
    processFlow(newEvent());
    assertProcessingStrategyProfiling();
    assertProcessingStrategyTracing();
  }

  private void spySchedulers() {
    cpuLight = spy(cpuLight);
    blocking = spy(blocking);
    cpuIntensive = spy(cpuIntensive);
  }
}
