/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.deployment.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.reset;
import static org.mule.runtime.deployment.model.api.domain.DomainDescriptor.DEFAULT_DOMAIN_NAME;
import static org.mule.runtime.module.deployment.internal.util.TestArtifactsRepository.callbackExtensionPlugin;
import static org.mule.runtime.module.deployment.internal.util.TestArtifactsRepository.dummyAppDescriptorFileBuilder;
import static org.mule.runtime.module.deployment.internal.util.TestArtifactsRepository.dummyDomainFileBuilder;
import static org.mule.runtime.module.deployment.internal.util.TestArtifactsRepository.emptyAppFileBuilder;
import static org.mule.test.allure.AllureConstants.ArtifactDeploymentFeature.DOMAIN_DEPLOYMENT;

import org.mule.runtime.module.deployment.impl.internal.builder.ApplicationFileBuilder;
import org.mule.runtime.module.deployment.impl.internal.builder.DomainBundleFileBuilder;
import org.mule.runtime.module.deployment.impl.internal.builder.DomainFileBuilder;

import org.junit.Test;

import io.qameta.allure.Feature;

/**
 * Contains test for domain bundle deployment
 */
@Feature(DOMAIN_DEPLOYMENT)
public class DomainBundleDeploymentTestCase extends AbstractDeploymentTestCase {

  public DomainBundleDeploymentTestCase(boolean parallelDeployment) {
    super(parallelDeployment);
  }

  @Test
  public void deploysDomainBundle() throws Exception {
    ApplicationFileBuilder applicationFileBuilder = new ApplicationFileBuilder(dummyAppDescriptorFileBuilder.get())
        .dependingOn(callbackExtensionPlugin.get()).dependingOn(dummyDomainFileBuilder.get());
    DomainBundleFileBuilder domainBundleFileBuilder =
        new DomainBundleFileBuilder(dummyDomainFileBuilder.get()).containing(applicationFileBuilder);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    startDeployment();

    assertDeploymentSuccess(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDeploymentSuccess(domainDeploymentListener, dummyDomainFileBuilder.get().getId());
    assertApplicationDeploymentSuccess(applicationDeploymentListener, dummyAppDescriptorFileBuilder.get().getId());

    assertDomainDir(NONE, new String[] {DEFAULT_DOMAIN_NAME, dummyDomainFileBuilder.get().getId()}, true);
    assertAppsDir(NONE, new String[] {dummyAppDescriptorFileBuilder.get().getId()}, true);
  }

  @Test
  public void failsToDeployDomainBundleWithCorruptedDomain() throws Exception {
    ApplicationFileBuilder applicationFileBuilder = new ApplicationFileBuilder(dummyAppDescriptorFileBuilder.get())
        .dependingOn(dummyDomainFileBuilder.get());
    DomainBundleFileBuilder domainBundleFileBuilder =
        new DomainBundleFileBuilder(new DomainFileBuilder("dummy-domain")).containing(applicationFileBuilder);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    startDeployment();

    assertDeploymentFailure(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDeploymentFailure(domainDeploymentListener, dummyDomainFileBuilder.get().getId());
    assertDeploymentFailure(applicationDeploymentListener, dummyAppDescriptorFileBuilder.get().getId());
  }

  @Test
  public void deploysDomainBundleWithCorruptedApp() throws Exception {
    ApplicationFileBuilder applicationFileBuilder = new ApplicationFileBuilder(dummyAppDescriptorFileBuilder.get()).corrupted();
    DomainBundleFileBuilder domainBundleFileBuilder =
        new DomainBundleFileBuilder(dummyDomainFileBuilder.get()).containing(applicationFileBuilder);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    startDeployment();

    assertDeploymentFailure(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDomainDir(NONE, new String[] {DEFAULT_DOMAIN_NAME, dummyDomainFileBuilder.get().getId()}, true);

    assertDeploymentSuccess(domainDeploymentListener, dummyDomainFileBuilder.get().getId());
    assertDeploymentFailure(applicationDeploymentListener, dummyAppDescriptorFileBuilder.get().getId());
  }

  @Test
  public void redeploysDomainBundle() throws Exception {
    ApplicationFileBuilder applicationFileBuilder = new ApplicationFileBuilder(dummyAppDescriptorFileBuilder.get())
        .dependingOn(dummyDomainFileBuilder.get()).dependingOn(callbackExtensionPlugin.get());
    DomainBundleFileBuilder domainBundleFileBuilder =
        new DomainBundleFileBuilder(dummyDomainFileBuilder.get()).containing(applicationFileBuilder);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    startDeployment();

    assertDeploymentSuccess(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDeploymentSuccess(domainDeploymentListener, dummyDomainFileBuilder.get().getId());
    assertApplicationDeploymentSuccess(applicationDeploymentListener, dummyAppDescriptorFileBuilder.get().getId());

    reset(domainDeploymentListener);
    reset(domainBundleDeploymentListener);
    reset(applicationDeploymentListener);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    assertDeploymentSuccess(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDomainRedeploymentSuccess(dummyDomainFileBuilder.get().getId());
    assertApplicationRedeploymentSuccess(dummyAppDescriptorFileBuilder.get().getId());
  }

  @Test
  public void redeploysDomainBundleCausesUndeployOfRemovedApps() throws Exception {
    ApplicationFileBuilder applicationFileBuilder1 = new ApplicationFileBuilder(dummyAppDescriptorFileBuilder.get())
        .dependingOn(callbackExtensionPlugin.get()).dependingOn(dummyDomainFileBuilder.get());
    ApplicationFileBuilder applicationFileBuilder2 = new ApplicationFileBuilder(emptyAppFileBuilder.get())
        .dependingOn(callbackExtensionPlugin.get()).dependingOn(dummyDomainFileBuilder.get());

    DomainBundleFileBuilder domainBundleFileBuilder = new DomainBundleFileBuilder(dummyDomainFileBuilder.get())
        .containing(applicationFileBuilder1).containing(applicationFileBuilder2);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    startDeployment();

    assertDeploymentSuccess(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDeploymentSuccess(domainDeploymentListener, dummyDomainFileBuilder.get().getId());
    assertApplicationDeploymentSuccess(applicationDeploymentListener, applicationFileBuilder1.getId());
    assertApplicationDeploymentSuccess(applicationDeploymentListener, applicationFileBuilder2.getId());

    reset(domainDeploymentListener);
    reset(domainBundleDeploymentListener);
    reset(applicationDeploymentListener);

    domainBundleFileBuilder = new DomainBundleFileBuilder(dummyDomainFileBuilder.get()).containing(applicationFileBuilder1);
    addDomainBundleFromBuilder(domainBundleFileBuilder);

    assertDeploymentSuccess(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDomainRedeploymentSuccess(dummyDomainFileBuilder.get().getId());
    assertApplicationRedeploymentSuccess(applicationFileBuilder1.getId());
    assertApplicationMissingOnBundleRedeployment(applicationFileBuilder2.getId());
  }

  @Test
  public void redeploysDomainBundleWithBrokenDomain() throws Exception {
    ApplicationFileBuilder applicationFileBuilder = new ApplicationFileBuilder(dummyAppDescriptorFileBuilder.get())
        .dependingOn(callbackExtensionPlugin.get()).dependingOn(dummyDomainFileBuilder.get());
    DomainBundleFileBuilder domainBundleFileBuilder =
        new DomainBundleFileBuilder(dummyDomainFileBuilder.get()).containing(applicationFileBuilder);

    addDomainBundleFromBuilder(domainBundleFileBuilder);

    startDeployment();

    assertDeploymentSuccess(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDeploymentSuccess(domainDeploymentListener, dummyDomainFileBuilder.get().getId());
    assertApplicationDeploymentSuccess(applicationDeploymentListener, dummyAppDescriptorFileBuilder.get().getId());

    reset(domainDeploymentListener);
    reset(domainBundleDeploymentListener);
    reset(applicationDeploymentListener);

    DomainFileBuilder corruptedDummyDomainFileBuilder = new DomainFileBuilder(dummyDomainFileBuilder.get()).corrupted();
    domainBundleFileBuilder = new DomainBundleFileBuilder(corruptedDummyDomainFileBuilder).containing(applicationFileBuilder);
    addDomainBundleFromBuilder(domainBundleFileBuilder);

    assertDeploymentFailure(domainBundleDeploymentListener, domainBundleFileBuilder.getId());
    assertDomainRedeploymentFailure(corruptedDummyDomainFileBuilder.getId());
    assertRedeploymentFailure(applicationDeploymentListener, dummyAppDescriptorFileBuilder.get().getId());
    assertThat(deploymentService.findApplication(dummyAppDescriptorFileBuilder.get().getId()), is(nullValue()));
  }

  private void addDomainBundleFromBuilder(DomainBundleFileBuilder domainBundleFileBuilder) throws Exception {
    addPackedDomainFromBuilder(domainBundleFileBuilder, null);
  }
}
