/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.internal;

import static org.mule.tck.util.MuleContextUtils.mockContextWithServices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.CustomizationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.module.deployment.api.DeploymentListener;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class MuleContextDeploymentListenerTestCase extends AbstractMuleTestCase {

  private static final String APP_NAME = "app";

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private final MuleContext muleContext = mockContextWithServices();

  @Mock
  private Registry registry;

  @Mock
  private CustomizationService customizationService;

  private final DeploymentListener deploymentListener = mock(DeploymentListener.class);
  private final MuleContextDeploymentListener contextListener = new MuleContextDeploymentListener(APP_NAME, deploymentListener);

  @Before
  public void before() {
    when(muleContext.getCustomizationService()).thenReturn(customizationService);
  }

  @Test
  public void notifiesMuleContextCreated() throws Exception {
    contextListener.onCreation(muleContext);
    verify(deploymentListener).onArtifactCreated(APP_NAME, customizationService);
  }

  @Test
  public void notifiesMuleContextInitialized() throws Exception {
    contextListener.onInitialization(muleContext, registry);

    verify(deploymentListener).onArtifactInitialised(APP_NAME, registry);
  }

  @Test
  public void notifiesMuleContextStart() throws Exception {
    contextListener.onStart(muleContext, registry);

    verify(deploymentListener).onArtifactStarted(APP_NAME, registry);
  }

  @Test
  public void notifiesMuleContextStop() throws Exception {
    contextListener.onStop(muleContext, registry);

    verify(deploymentListener).onArtifactStopped(APP_NAME, registry);
  }
}
