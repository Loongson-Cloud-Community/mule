/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.activation.api.extension.discovery;

import static java.util.Collections.unmodifiableSet;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.api.extension.provider.RuntimeExtensionModelProvider;
import org.mule.runtime.module.artifact.activation.api.plugin.PluginClassLoaderSupplier;
import org.mule.runtime.module.artifact.activation.internal.extension.discovery.DefaultExtensionModelDiscoverer;
import org.mule.runtime.module.artifact.activation.internal.extension.discovery.RepositoryLookupExtensionModelGenerator;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;

import java.util.Set;

/**
 * Provides a way to discover {@link ExtensionModel}s from the Mule Runtime or from {@code mule-plugins} in the context of a
 * deployable artifact.
 * 
 * @since 4.5
 */
@NoImplement
public interface ExtensionModelDiscoverer {

  /**
   * Creates an {@link ExtensionModelDiscoverer} that will generate the extension models for plugins in a class loader.
   * 
   * @param classLoaderFactory             a way to obtain the class loader for a given plugin.
   * @param extensionModelLoaderRepository repository to manage access to an
   *                                       {@link org.mule.runtime.extension.api.loader.ExtensionModelLoader}.
   * @return a newly created {@link ExtensionModelDiscoverer}.
   */
  static ExtensionModelDiscoverer defaultExtensionModelDiscoverer(PluginClassLoaderSupplier classLoaderFactory,
                                                                  ExtensionModelLoaderRepository extensionModelLoaderRepository) {
    return new DefaultExtensionModelDiscoverer(new RepositoryLookupExtensionModelGenerator(classLoaderFactory,
                                                                                           extensionModelLoaderRepository));
  }

  /**
   * Creates an {@link ExtensionModelDiscoverer} that will generate the extension models for plugins in a class loader.
   *
   * @param applicationClassLoader         class loader of the application containing the plugin class loaders needed for the
   *                                       discovery.
   * @param extensionModelLoaderRepository repository to manage access to an
   *                                       {@link org.mule.runtime.extension.api.loader.ExtensionModelLoader}.
   * @return a newly created {@link ExtensionModelDiscoverer}.
   */
  static ExtensionModelDiscoverer defaultExtensionModelDiscoverer(MuleDeployableArtifactClassLoader applicationClassLoader,
                                                                  ExtensionModelLoaderRepository extensionModelLoaderRepository) {
    return new DefaultExtensionModelDiscoverer(new RepositoryLookupExtensionModelGenerator(artifactPluginDescriptor -> applicationClassLoader
        .getArtifactPluginClassLoaders().stream()
        .filter(apcl -> apcl.getArtifactDescriptor().getBundleDescriptor().getGroupId()
            .equals(artifactPluginDescriptor.getBundleDescriptor().getGroupId())
            && apcl.getArtifactDescriptor().getBundleDescriptor().getArtifactId()
                .equals(artifactPluginDescriptor.getBundleDescriptor().getArtifactId()))
        .findAny().get(), extensionModelLoaderRepository));
  }

  /**
   * Discovers the extension models provided by the Mule Runtime.
   *
   * @return {@link Set} of the runtime provided {@link ExtensionModel}s.
   * 
   * @deprecated since 4.5 use {@link RuntimeExtensionModelProvider#discoverRuntimeExtensionModels()} instead.
   */
  @Deprecated
  static Set<ExtensionModel> discoverRuntimeExtensionModels() {
    return unmodifiableSet(RuntimeExtensionModelProvider.discoverRuntimeExtensionModels());
  }

  /**
   * For each artifactPlugin discovers the {@link ExtensionModel}.
   *
   * @param discoveryRequest an object containing the parameterization of the discovery process.
   * @return The discovered {@link ExtensionModel}s.
   */
  Set<ExtensionModel> discoverPluginsExtensionModels(ExtensionDiscoveryRequest discoveryRequest);

}
