/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.artifact;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import org.mule.runtime.core.api.util.StringUtils;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoaderManager;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks all the {@link ArtifactClassLoader} created on the container.
 */
public class DefaultClassLoaderManager implements ArtifactClassLoaderManager, ClassLoaderRepository {

  private final Map<String, ArtifactClassLoader> artifactClassLoaders = new ConcurrentHashMap<>();

  @Override
  public void register(ArtifactClassLoader artifactClassLoader) {

    checkArgument(artifactClassLoader != null, "artifactClassLoader cannot be null");

    artifactClassLoaders.put(artifactClassLoader.getArtifactId(), artifactClassLoader);
  }

  @Override
  public ArtifactClassLoader unregister(String classLoaderId) {
    checkClassLoaderId(classLoaderId);

    return artifactClassLoaders.remove(classLoaderId);
  }

  @Override
  public Optional<ClassLoader> find(String classLoaderId) {
    checkClassLoaderId(classLoaderId);

    ArtifactClassLoader artifactClassLoader = artifactClassLoaders.get(classLoaderId);

    return artifactClassLoader != null ? of(artifactClassLoader.getClassLoader()) : empty();
  }

  @Override
  public Optional<String> getId(ClassLoader classLoader) {
    return artifactClassLoaders.values().stream()
        .filter(artifactClassLoader -> artifactClassLoader.getClassLoader().equals(classLoader))
        .findFirst()
        .map(ArtifactClassLoader::getArtifactId);
  }

  private void checkClassLoaderId(String classLoaderId) {
    checkArgument(!StringUtils.isEmpty(classLoaderId), "artifactId cannot be empty");
  }
}
