/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.classloader;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.api.annotation.NoInstantiate;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.util.List;

/**
 * Tracks {@link ArtifactClassLoader} created by {@link DeployableArtifactClassLoaderFactory}
 */
@NoInstantiate
public final class TrackingDeployableArtifactClassLoaderFactory<T extends ArtifactDescriptor>
    implements DeployableArtifactClassLoaderFactory<T> {

  private final ArtifactClassLoaderManager artifactClassLoaderManager;
  private final DeployableArtifactClassLoaderFactory<T> artifactClassLoaderFactory;

  /**
   * Tracks the classloader created by another factory
   *
   * @param artifactClassLoaderManager tracks each created class loader. Non null.
   * @param artifactClassLoaderFactory factory that creates the class loaders to be tracked. Non null.
   */
  public TrackingDeployableArtifactClassLoaderFactory(ArtifactClassLoaderManager artifactClassLoaderManager,
                                                      DeployableArtifactClassLoaderFactory<T> artifactClassLoaderFactory) {
    checkArgument(artifactClassLoaderManager != null, "artifactClassLoaderManager cannot be null");
    checkArgument(artifactClassLoaderFactory != null, "artifactClassLoaderFactory cannot be null");
    this.artifactClassLoaderManager = artifactClassLoaderManager;
    this.artifactClassLoaderFactory = artifactClassLoaderFactory;
  }

  @Override
  public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, T descriptor) {
    ArtifactClassLoader artifactClassLoader =
        artifactClassLoaderFactory.create(artifactId, parent, descriptor);

    track(artifactClassLoader);

    return artifactClassLoader;
  }

  @Override
  public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, T descriptor,
                                    List<ArtifactClassLoader> artifactPluginClassLoaders) {
    return create(artifactId, parent, descriptor);
  }

  private void track(ArtifactClassLoader artifactClassLoader) {
    artifactClassLoaderManager.register(artifactClassLoader);
    artifactClassLoader.addShutdownListener(() -> artifactClassLoaderManager.unregister(artifactClassLoader.getArtifactId()));
  }

}
