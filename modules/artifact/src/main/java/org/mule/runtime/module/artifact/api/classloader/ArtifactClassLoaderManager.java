/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.classloader;

import org.mule.api.annotation.NoImplement;

/**
 * Tracks {@link ArtifactClassLoader} instances created on the container.
 */
@NoImplement
public interface ArtifactClassLoaderManager {

  /**
   * Registers a new class loader
   *
   * @param artifactClassLoader created classloader. Non null
   */
  void register(ArtifactClassLoader artifactClassLoader);

  /**
   * Un-registers a disposed class loader.
   *
   * @param classLoaderId identifier for the classloader to be unregistered. Non empty.
   * @return the classloader registered under the given ID, null if no class loader with that ID was registered before.
   */
  ArtifactClassLoader unregister(String classLoaderId);
}
