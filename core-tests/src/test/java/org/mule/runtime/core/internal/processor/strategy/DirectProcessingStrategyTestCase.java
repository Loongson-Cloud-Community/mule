/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.processor.strategy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE;
import static org.mule.runtime.core.api.transaction.TransactionCoordination.getInstance;
import static org.mule.tck.util.MuleContextUtils.getNotificationDispatcher;
import static org.mule.test.allure.AllureConstants.ProcessingStrategiesFeature.PROCESSING_STRATEGIES;
import static org.mule.test.allure.AllureConstants.ProcessingStrategiesFeature.ProcessingStrategiesStory.DIRECT;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.tck.testmodels.mule.TestTransaction;

import org.hamcrest.Matcher;
import org.junit.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(PROCESSING_STRATEGIES)
@Story(DIRECT)
public class DirectProcessingStrategyTestCase extends AbstractProcessingStrategyTestCase {

  public DirectProcessingStrategyTestCase(Mode mode, boolean profiling) {
    // The blocking processing strategy does not implement profiling yet
    super(mode, false);
  }

  @Override
  protected ProcessingStrategy createProcessingStrategy(MuleContext muleContext, String schedulersNamePrefix) {
    return new DirectProcessingStrategyFactory().create(muleContext, schedulersNamePrefix);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleCpuLight() throws Exception {
    super.singleCpuLight();
    assertSynchronous(1);
  }

  @Override
  protected Matcher<Iterable<? extends String>> cpuLightSchedulerMatcher() {
    return emptyIterable();
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleCpuLightConcurrent() throws Exception {
    super.internalConcurrent(flowBuilder.get(), false, CPU_LITE, 1);
    assertSynchronous(2);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void multipleCpuLight() throws Exception {
    super.multipleCpuLight();
    assertSynchronous(1);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleBlocking() throws Exception {
    super.singleBlocking();
    assertSynchronous(1);
  }

  @Override
  protected Matcher<Iterable<? extends String>> ioSchedulerMatcher() {
    return emptyIterable();
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void multipleBlocking() throws Exception {
    super.multipleBlocking();
    assertSynchronous(1);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleCpuIntensive() throws Exception {
    super.singleCpuIntensive();
    assertSynchronous(1);
  }

  @Override
  protected Matcher<Iterable<? extends String>> cpuIntensiveSchedulerMatcher() {
    return emptyIterable();
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void multipleCpuIntensive() throws Exception {
    super.multipleCpuIntensive();
    assertSynchronous(1);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void mix() throws Exception {
    super.mix();
    assertSynchronous(1);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void mix2() throws Exception {
    super.mix2();
    assertSynchronous(1);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void tx() throws Exception {
    flow = flowBuilder.get().processors(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor).build();
    startFlow();

    getInstance()
        .bindTransaction(new TestTransaction("appName", getNotificationDispatcher(muleContext)));

    processFlow(testEvent());

    assertSynchronous(1);
  }

  @Override
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread but async processors will cause additional threads to be used. Flow processing "
      + "continues using async processor thread.")
  public void asyncCpuLight() throws Exception {
    super.asyncCpuLight();
    assertAsyncCpuLight();
  }

  @Test
  @Description("Regardless of processor type, when the DirectProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleIORW() throws Exception {
    super.singleIORW(() -> testEvent(), emptyIterable());
    assertSynchronous(1);
  }

  @Override
  @Description("When using DirectProcessingStrategy continued processing is carried out using async processor thread which can "
      + "cause processing to block if there are concurrent requests and the number of custom async processor threads are reduced")
  public void asyncCpuLightConcurrent() throws Exception {
    internalConcurrent(flowBuilder.get(), true, CPU_LITE, 4, asyncProcessor);
    assertThat(threads.size(), between(2, 9));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads.stream().filter(name -> name.startsWith(CUSTOM)).count(), equalTo(4l));
  }

  protected void assertAsyncCpuLight() {
    assertThat(threads, hasSize(2));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads.stream().filter(name -> name.startsWith(CUSTOM)).count(), equalTo(1l));
  }

  protected void assertSynchronous(int concurrency) {
    assertThat(threads, hasSize(concurrency));
    assertThat(threads, not(hasItem(startsWith(CPU_LIGHT))));
    assertThat(threads, not(hasItem(startsWith(IO))));
    assertThat(threads, not(hasItem(startsWith(CPU_INTENSIVE))));
    assertThat(threads, not(hasItem(startsWith(CUSTOM))));
  }

}
