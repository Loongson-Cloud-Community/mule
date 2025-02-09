/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.core.api.retry.policy.RetryPolicyTemplate;
import org.mule.runtime.core.api.retry.async.AsynchronousRetryTemplate;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.mockito.Mockito;

public class LifecycleAwareConfigurationInstanceAsyncRetryTestCase extends LifecycleAwareConfigurationInstanceTestCase {

  public LifecycleAwareConfigurationInstanceAsyncRetryTestCase(String name, ConnectionProvider connectionProvider) {
    super(name, connectionProvider);
  }

  @Override
  protected RetryPolicyTemplate createRetryTemplate() {
    return new AsynchronousRetryTemplate(super.createRetryTemplate());
  }

  @Override
  @Test
  public void testConnectivityFailsUponStart() throws Exception {
    if (connectionProvider.isPresent()) {
      Exception connectionException = new ConnectionException("Oops!");
      when(connectionManager.testConnectivity(configurationInstance))
          .thenReturn(failure(connectionException.getMessage(), connectionException));

      configurationInstance.initialise();
      configurationInstance.start();

      new PollingProber().check(new JUnitLambdaProbe(() -> {
        verify(connectionManager, times(RECONNECTION_MAX_ATTEMPTS + 1)).testConnectivity(configurationInstance);
        return true;
      }));
    }
  }

  @Override
  @Test
  public void valueStarted() throws Exception {
    configurationInstance.initialise();
    super.valueStarted();
  }

  @Override
  @Test
  public void valueStopped() throws Exception {
    configurationInstance.initialise();
    super.valueStopped();
  }

  @Override
  @Test
  public void connectionUnbound() throws Exception {
    configurationInstance.initialise();
    super.connectionUnbound();
  }

  @Override
  @Test
  public void testConnectivityUponStart() throws Exception {
    if (connectionProvider.isPresent()) {
      valueStarted();
      new PollingProber().check(new JUnitLambdaProbe(() -> {
        verify(connectionManager).testConnectivity(configurationInstance);
        return true;
      }));
    }
  }

  @Test
  public void stopWhileConnectivityTestingExecuting() throws Throwable {
    if (connectionProvider.isPresent()) {
      final Latch testConnectivityInvokedLatch = new Latch();
      final Latch interceptableShutdownLatch = new Latch();

      reset(connectionManager);

      AtomicBoolean stopped = new AtomicBoolean();
      AtomicReference<Throwable> thrownByTestConnectivity = new AtomicReference<>();
      AtomicBoolean testConnectivityFinished = new AtomicBoolean();
      when(connectionManager.getRetryTemplateFor(connectionProvider.get())).thenReturn(retryPolicyTemplate);
      when(connectionManager.testConnectivity(Mockito.any(ConfigurationInstance.class))).then(inv -> {
        testConnectivityInvokedLatch.countDown();

        try {
          interceptableShutdownLatch.await(10, SECONDS);
          assertThat(stopped.get(), is(false));
        } catch (Throwable t) {
          thrownByTestConnectivity.set(t);
        }
        testConnectivityFinished.set(true);
        return success();
      });

      configurationInstance.initialise();
      configurationInstance.start();
      assertThat(testConnectivityInvokedLatch.await(10, SECONDS), is(true));

      configurationInstance.stop();
      stopped.set(true);
      interceptableShutdownLatch.countDown();

      new PollingProber(15000, 1000).check(new JUnitLambdaProbe(testConnectivityFinished::get));

      if (thrownByTestConnectivity.get() != null) {
        throw thrownByTestConnectivity.get();
      }
    }
  }

}
