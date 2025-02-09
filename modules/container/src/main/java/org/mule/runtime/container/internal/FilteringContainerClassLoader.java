/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.container.internal;

import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderFilter;
import org.mule.runtime.module.artifact.api.classloader.ExportedService;
import org.mule.runtime.module.artifact.api.classloader.FilteringArtifactClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * Filtering artifact classLoader that to use as the parent classloader for all mule tops artifact (domains, server plugins, etc).
 * <p>
 * Differs from the base class is that exposes all the resources available in the delegate classLoader and the delegate's parent
 * classLoader.
 */
public class FilteringContainerClassLoader extends FilteringArtifactClassLoader {

  static {
    registerAsParallelCapable();
  }

  /**
   * Creates a new instance
   *
   * @param parent               The parent class loader
   * @param containerClassLoader delegate classLoader. Not null.
   * @param filter               filter used to determine which classes and resources are exported on the delegate classLoader.
   * @param exportedServices     service providers that will be available from the filtered class loader. Non null.
   */
  public FilteringContainerClassLoader(ClassLoader parent, ArtifactClassLoader containerClassLoader, ClassLoaderFilter filter,
                                       List<ExportedService> exportedServices) {
    super(parent, containerClassLoader, filter, exportedServices);
  }

  /**
   * Creates a new instance
   *
   * @param containerClassLoader delegate classLoader. Not null.
   * @param filter               filter used to determine which classes and resources are exported on the delegate classLoader.
   * @param exportedServices     service providers that will be available from the filtered class loader. Non null.
   */
  public FilteringContainerClassLoader(ArtifactClassLoader containerClassLoader, ClassLoaderFilter filter,
                                       List<ExportedService> exportedServices) {
    super(containerClassLoader, filter, exportedServices);
  }

  @Override
  protected URL getResourceFromDelegate(ArtifactClassLoader artifactClassLoader, String name) {
    return artifactClassLoader.getClassLoader().getResource(name);
  }

  @Override
  protected Enumeration<URL> getResourcesFromDelegate(ArtifactClassLoader artifactClassLoader, String name) throws IOException {
    return artifactClassLoader.getClassLoader().getResources(name);
  }
}
