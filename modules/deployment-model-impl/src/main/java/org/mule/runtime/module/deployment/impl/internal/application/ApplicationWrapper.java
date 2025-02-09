/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.application;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.connectivity.ConnectivityTestingService;
import org.mule.runtime.api.metadata.MetadataService;
import org.mule.runtime.api.value.ValueProviderService;
import org.mule.runtime.core.api.data.sample.SampleDataService;
import org.mule.runtime.deployment.model.api.application.Application;
import org.mule.runtime.deployment.model.api.application.ApplicationPolicyManager;
import org.mule.runtime.deployment.model.api.application.ApplicationStatus;
import org.mule.runtime.deployment.model.api.domain.Domain;
import org.mule.runtime.module.artifact.api.classloader.RegionClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import org.mule.runtime.module.deployment.impl.internal.artifact.DeployableArtifactWrapper;

import java.io.File;
import java.io.IOException;

/**
 * Decorates the target deployer to properly switch out context classloader for deployment one where applicable. E.g. init() phase
 * may load custom classes for an application, which must be executed with deployment (app) classloader in the context, and not
 * Mule system classloader.
 */
public class ApplicationWrapper extends DeployableArtifactWrapper<Application, ApplicationDescriptor> implements Application {

  protected ApplicationWrapper(Application delegate) throws IOException {
    super(delegate);
  }

  @Override
  public String getAppName() {
    return getArtifactName();
  }

  @Override
  public Registry getRegistry() {
    return getDelegate().getArtifactContext() != null ? getDelegate().getArtifactContext().getRegistry() : null;
  }

  @Override
  public Domain getDomain() {
    return getDelegate().getDomain();
  }

  @Override
  public ApplicationStatus getStatus() {
    return getDelegate().getStatus();
  }

  @Override
  public RegionClassLoader getRegionClassLoader() {
    return getDelegate().getRegionClassLoader();
  }

  @Override
  public ApplicationPolicyManager getPolicyManager() {
    return getDelegate().getPolicyManager();
  }

  @Override
  public String toString() {
    return String.format("%s(%s)", getClass().getName(), getDelegate());
  }

  @Override
  public Application getDelegate() {
    return super.getDelegate();
  }

  @Override
  public File getLocation() {
    return getDelegate().getLocation();
  }

  @Override
  public ConnectivityTestingService getConnectivityTestingService() {
    return getDelegate().getConnectivityTestingService();
  }

  @Override
  public MetadataService getMetadataService() {
    return getDelegate().getMetadataService();
  }

  @Override
  public ValueProviderService getValueProviderService() {
    return getDelegate().getValueProviderService();
  }

  @Override
  public SampleDataService getSampleDataService() {
    return getDelegate().getSampleDataService();
  }
}
