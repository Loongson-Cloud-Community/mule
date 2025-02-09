/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.internal;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.SERVICE;
import static org.mule.runtime.module.deployment.internal.DeploymentListenerAdapter.UNSUPPORTED_ARTIFACT_TYPE_ERROR;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.CustomizationService;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.deployment.api.ArtifactDeploymentListener;
import org.mule.runtime.module.deployment.api.DeploymentListener;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DeploymentListenerAdapterTestCase extends AbstractMuleTestCase {

  private static final String ARTIFACT_NAME = "artifactName";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private ArtifactDeploymentListener listener;

  @Mock
  private CustomizationService customizationService;

  @Mock
  private Registry registry;


  @Test
  public void adapt() throws Exception {
    DeploymentListener applicationDeploymentListener = new DeploymentListenerAdapter(listener, APP);
    DeploymentListener domainDeploymentListener = new DeploymentListenerAdapter(listener, DOMAIN);
    assertDeploymentListenerInvocations(applicationDeploymentListener, APP);
    assertDeploymentListenerInvocations(domainDeploymentListener, DOMAIN);
  }

  @Test
  public void invalidArtifactType() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(UNSUPPORTED_ARTIFACT_TYPE_ERROR);
    new DeploymentListenerAdapter(listener, SERVICE);
  }

  private void assertDeploymentListenerInvocations(DeploymentListener deploymentListener, ArtifactType artifactType) {
    notifyDeploymentEvents(deploymentListener);
    verifyArtifactDeploymentListenerExecutions(artifactType);
    reset(listener);
  }

  private void notifyDeploymentEvents(DeploymentListener deploymentListener) {
    deploymentListener.onDeploymentStart(ARTIFACT_NAME);
    deploymentListener.onDeploymentFailure(ARTIFACT_NAME, null);
    deploymentListener.onDeploymentSuccess(ARTIFACT_NAME);
    deploymentListener.onUndeploymentStart(ARTIFACT_NAME);
    deploymentListener.onUndeploymentFailure(ARTIFACT_NAME, null);
    deploymentListener.onUndeploymentSuccess(ARTIFACT_NAME);
    deploymentListener.onArtifactCreated(ARTIFACT_NAME, customizationService);
    deploymentListener.onArtifactInitialised(ARTIFACT_NAME, registry);
  }

  private void verifyArtifactDeploymentListenerExecutions(ArtifactType artifactType) {
    verify(listener).onDeploymentStart(artifactType, ARTIFACT_NAME);
    verify(listener).onDeploymentFailure(artifactType, ARTIFACT_NAME, null);
    verify(listener).onDeploymentSuccess(artifactType, ARTIFACT_NAME);
    verify(listener).onUndeploymentStart(artifactType, ARTIFACT_NAME);
    verify(listener).onUndeploymentFailure(artifactType, ARTIFACT_NAME, null);
    verify(listener).onUndeploymentSuccess(artifactType, ARTIFACT_NAME);
    verify(listener).onArtifactCreated(artifactType, ARTIFACT_NAME, customizationService);
    verify(listener).onArtifactInitialised(artifactType, ARTIFACT_NAME, registry);
  }

}
