/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.descriptor;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;

import java.util.Optional;

/**
 * Maintains the registered {@link DescriptorLoader}
 */
@NoImplement
public interface DescriptorLoaderRepository {

  /**
   * Gets a descriptor loader from the repository
   *
   * @param id          identifies the loader to obtain. Non empty.
   * @param loaderClass class of {@link DescriptorLoader} to search for. No null.
   * @param <T>         type of descriptor loader to return
   * @returns a non null {@link Optional} loader of the given class and ID
   * @throws LoaderNotFoundException if there is no registered loader of type {@link T} with the provided ID.
   */
  <T extends DescriptorLoader> T get(String id, ArtifactType artifactType, Class<T> loaderClass) throws LoaderNotFoundException;
}
