/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.domain;

import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;
import static org.mule.runtime.module.artifact.api.descriptor.DomainDescriptor.DEFAULT_CONFIGURATION_RESOURCE;

import org.mule.runtime.api.deployment.meta.MuleDomainModel;
import org.mule.runtime.api.deployment.persistence.AbstractMuleArtifactModelJsonSerializer;
import org.mule.runtime.api.deployment.persistence.MuleDomainModelJsonSerializer;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import org.mule.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import org.mule.runtime.module.artifact.api.descriptor.DomainDescriptor;
import org.mule.runtime.module.deployment.impl.internal.artifact.AbstractDeployableDescriptorFactory;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

/**
 * Creates artifact descriptor for application
 */
public class DomainDescriptorFactory extends AbstractDeployableDescriptorFactory<MuleDomainModel, DomainDescriptor> {

  public DomainDescriptorFactory(ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader,
                                 DescriptorLoaderRepository descriptorLoaderRepository,
                                 ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(artifactPluginDescriptorLoader, descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
  }


  @Override
  protected String getDefaultConfigurationResource() {
    return DEFAULT_CONFIGURATION_RESOURCE;
  }

  @Override
  protected ArtifactType getArtifactType() {
    return DOMAIN;
  }

  @Override
  protected AbstractMuleArtifactModelJsonSerializer<MuleDomainModel> getMuleArtifactModelJsonSerializer() {
    return new MuleDomainModelJsonSerializer();
  }

  @Override
  protected void doDescriptorConfig(MuleDomainModel artifactModel, DomainDescriptor descriptor, File artifactLocation) {
    super.doDescriptorConfig(artifactModel, descriptor, artifactLocation);
  }

  @Override
  protected DomainDescriptor createArtifactDescriptor(File artifactLocation, String name,
                                                      Optional<Properties> deploymentProperties) {
    // Keep compatibility with usages of the factory that expect the descriptor from previous version.
    return new org.mule.runtime.deployment.model.api.domain.DomainDescriptor(artifactLocation.getName(), deploymentProperties);
  }
}
