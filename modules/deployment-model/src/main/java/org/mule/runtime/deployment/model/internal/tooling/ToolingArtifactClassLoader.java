/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.deployment.model.internal.tooling;

import static org.mule.runtime.api.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.classloader.RegionClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ShutdownListener;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

/**
 * Tooling {@link ClassLoader} that will delegate every call to it's delegate (the specific
 * {@link org.eclipse.aether.artifact.Artifact} under {@link #delegateArtifactClassLoader}, but when doing the {@link #dispose()}
 * it will dispatch to the {@link RegionClassLoader} pointed by {@link #regionClassLoader} that contains all the related class
 * loaders in it.
 *
 * @since 4.0
 */
public class ToolingArtifactClassLoader implements ArtifactClassLoader {

  private final RegionClassLoader regionClassLoader;
  private final ArtifactClassLoader delegateArtifactClassLoader;

  /**
   * Generates an instance of an {@link ArtifactClassLoader} if the parametrized {@code regionClassLoader} does contain within its
   * {@link RegionClassLoader#getArtifactPluginClassLoaders()} the class loader responsible of handling the
   * {@code artifactPluginDescriptor}.
   *
   * @param regionClassLoader           class loader used to execute the {@link #dispose()} properly.
   * @param delegateArtifactClassLoader {@link ArtifactClassLoader} where this classloader should delegate.
   */
  public ToolingArtifactClassLoader(RegionClassLoader regionClassLoader,
                                    ArtifactClassLoader delegateArtifactClassLoader) {
    checkNotNull(regionClassLoader, "regionClassLoader cannot be null");
    checkNotNull(delegateArtifactClassLoader, "delegateArtifactClassLoader cannot be null");

    this.regionClassLoader = regionClassLoader;
    this.delegateArtifactClassLoader = delegateArtifactClassLoader;
  }

  /**
   * @return {@link List} for {@link ArtifactClassLoader} for the plugins of the region.
   */
  public List<ArtifactClassLoader> getArtifactPluginClassLoaders() {
    return regionClassLoader.getArtifactPluginClassLoaders();
  }

  /**
   * @return {@link RegionClassLoader} that this artifact class loader belongs too.
   */
  public RegionClassLoader getRegionClassLoader() {
    return this.regionClassLoader;
  }

  @Override
  public String getArtifactId() {
    return delegateArtifactClassLoader.getArtifactId();
  }

  @Override
  public <T extends ArtifactDescriptor> T getArtifactDescriptor() {
    return delegateArtifactClassLoader.getArtifactDescriptor();
  }

  @Override
  public URL findResource(String resource) {
    return delegateArtifactClassLoader.findResource(resource);
  }

  @Override
  public URL findInternalResource(String resource) {
    return delegateArtifactClassLoader.findInternalResource(resource);
  }

  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    return delegateArtifactClassLoader.findResources(name);
  }

  @Override
  public Class<?> findLocalClass(String name) throws ClassNotFoundException {
    return delegateArtifactClassLoader.findLocalClass(name);
  }

  @Override
  public Class<?> loadInternalClass(String name) throws ClassNotFoundException {
    return delegateArtifactClassLoader.loadInternalClass(name);
  }

  @Override
  public ClassLoader getClassLoader() {
    return delegateArtifactClassLoader.getClassLoader();
  }

  @Override
  public void addShutdownListener(ShutdownListener listener) {
    delegateArtifactClassLoader.addShutdownListener(listener);
  }

  @Override
  public ClassLoaderLookupPolicy getClassLoaderLookupPolicy() {
    return delegateArtifactClassLoader.getClassLoaderLookupPolicy();
  }

  @Override
  public URL findLocalResource(String resourceName) {
    return delegateArtifactClassLoader.findLocalResource(resourceName);
  }

  /**
   * We want tooling believe the {@link ArtifactClassLoader} he's handling is the plugin's or application's one, but we are
   * actually shipping more than that with the {@link RegionClassLoader}.
   * <p>
   * So, to avoid any leaks the {@link #dispose()} of the plugin's {@link ArtifactClassLoader} is actually the one for the
   * {@link RegionClassLoader}, which will eventually execute a {@link #dispose()} over the plugin's or application's one.
   */
  @Override
  public void dispose() {
    regionClassLoader.dispose();
  }
}
