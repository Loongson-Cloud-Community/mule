/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_CONNECTION_MANAGER;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_TIME_SUPPLIER;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.config.PoolingProfile;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.internal.config.builders.MinimalConfigurationBuilder;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.retry.RetryNotifier;
import org.mule.runtime.core.api.retry.async.AsynchronousRetryTemplate;
import org.mule.runtime.core.api.retry.policy.RetryPolicyTemplate;
import org.mule.runtime.core.api.retry.policy.SimpleRetryPolicyTemplate;
import org.mule.runtime.core.internal.connection.ConnectionManagerAdapter;
import org.mule.runtime.core.internal.retry.ReconnectionConfig;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.config.ConfigurationState;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.size.SmallTest;
import org.mule.tck.util.TestTimeSupplier;

import java.util.Map;
import java.util.Optional;

import io.qameta.allure.Description;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class AsyncTestConnectivityTestCase extends AbstractMuleContextTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  protected static final int RECONNECTION_MAX_ATTEMPTS = 5;
  private static final int RECONNECTION_FREQ = 100;
  private static final String NAME = "name";
  private static final int TEST_TIMEOUT = 2000;
  private static final int TEST_POLL_DELAY = 10;

  public AsyncTestConnectivityTestCase() {}

  @Mock
  private ConfigurationModel configurationModel;

  @Mock
  private ConfigurationState configurationState;

  protected Lifecycle value = mock(Lifecycle.class, withSettings().extraInterfaces(Component.class));
  protected AsyncConnectionManagerAdapter connectionManager;
  protected RetryPolicyTemplate retryPolicyTemplate;
  protected LifecycleAwareConfigurationInstance configurationInstance;
  private final TestTimeSupplier timeSupplier = new TestTimeSupplier(currentTimeMillis());
  private final Optional<ConnectionProvider> connectionProvider =
      of(mock(ConnectionProvider.class, withSettings().extraInterfaces(Lifecycle.class, MuleContextAware.class)));

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(OBJECT_TIME_SUPPLIER, timeSupplier);
  }

  @Override
  protected ConfigurationBuilder getBuilder() throws Exception {
    return new MinimalConfigurationBuilder() {

      @Override
      protected void doConfigure(MuleContext muleContext) throws Exception {
        super.doConfigure(muleContext);

        retryPolicyTemplate = createRetryTemplate();
        retryPolicyTemplate.setNotifier(mock(RetryNotifier.class));
        connectionManager = spy(new AsyncConnectionManagerAdapter(retryPolicyTemplate));

        registerObject(OBJECT_CONNECTION_MANAGER, connectionManager, muleContext);
      }
    };
  }

  @Override
  protected void doSetUp() throws Exception {
    configurationInstance = createConfigurationInstance();

    super.doSetUp();
  }

  protected RetryPolicyTemplate createRetryTemplate() {
    return new AsynchronousRetryTemplate(new SimpleRetryPolicyTemplate(RECONNECTION_FREQ, RECONNECTION_MAX_ATTEMPTS));
  }

  @After
  public void after() {
    configurationInstance.dispose();
  }

  protected LifecycleAwareConfigurationInstance createConfigurationInstance() throws MuleException {
    return muleContext.getInjector().inject(new LifecycleAwareConfigurationInstance(NAME,
                                                                                    configurationModel,
                                                                                    value,
                                                                                    configurationState,
                                                                                    connectionProvider));
  }


  @Test
  @Description("Checks that the test connectivity test is not interrupted")
  public void testConnectivityIsNotInterruptedWhenAsyncRetryTemplate() throws Exception {
    configurationInstance.initialise();
    configurationInstance.start();
    new PollingProber(TEST_TIMEOUT, TEST_POLL_DELAY).check(new JUnitLambdaProbe(() -> {
      verify(connectionManager).testConnectivity(configurationInstance);
      assertThat(connectionManager.wasInterruptedBeforeTestingConnectivity(), is(false));
      return true;
    }));
  }

  /**
   * This class is used to verify that the testConnectivity is performed before the thread pool used for connectivity testing
   * purposes is interrupted.
   */
  private static class AsyncConnectionManagerAdapter implements ConnectionManagerAdapter {

    private final RetryPolicyTemplate retryPolicyTemplate;

    public AsyncConnectionManagerAdapter(RetryPolicyTemplate retryPolicyTemplate) {
      this.retryPolicyTemplate = retryPolicyTemplate;
    }

    private boolean interrupted = false;

    @Override
    public <C> void bind(Object config, ConnectionProvider<C> connectionProvider) {}

    @Override
    public boolean hasBinding(Object config) {
      return false;
    }

    @Override
    public void unbind(Object config) {}

    @Override
    public <C> ConnectionHandler<C> getConnection(Object config) throws ConnectionException {
      return null;
    }

    @Override
    public <C> ConnectionValidationResult testConnectivity(ConnectionProvider<C> connectionProvider) {
      if (Thread.currentThread().isInterrupted()) {
        interrupted = true;
      }

      return success();
    }

    @Override
    public <C> ConnectionValidationResult testConnectivity(C connection, ConnectionHandler<C> connectionHandler) {
      return success();
    }

    @Override
    public ConnectionValidationResult testConnectivity(ConfigurationInstance configurationInstance)
        throws IllegalArgumentException {
      return success();
    }

    @Override
    public void initialise() throws InitialisationException {}

    @Override
    public void start() throws MuleException {}

    @Override
    public void stop() throws MuleException {}

    @Override
    public void dispose() {}

    @Override
    public <C> RetryPolicyTemplate getRetryTemplateFor(ConnectionProvider<C> connectionProvider) {
      return retryPolicyTemplate;
    }

    @Override
    public <C> ReconnectionConfig getReconnectionConfigFor(ConnectionProvider<C> connectionProvider) {
      return null;
    }

    @Override
    public PoolingProfile getDefaultPoolingProfile() {
      return null;
    }

    public boolean wasInterruptedBeforeTestingConnectivity() {
      return interrupted;
    }

  }
}
