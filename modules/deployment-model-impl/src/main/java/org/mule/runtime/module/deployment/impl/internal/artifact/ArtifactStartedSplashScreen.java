/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.artifact;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import org.mule.runtime.core.internal.util.splash.SplashScreen;
import org.mule.runtime.module.artifact.api.Artifact;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base splash screen to log messages when an {@link Artifact} is started based on it's {@link ArtifactDescriptor}.
 *
 * @param <D> the type of {@link ArtifactDescriptor}
 */
public abstract class ArtifactStartedSplashScreen<D extends ArtifactDescriptor> extends SplashScreen {

  protected abstract void createMessage(D descriptor);

  protected List<String> getLibraries(File artifactLibFolder) {
    if (artifactLibFolder.exists()) {
      String[] libraries = artifactLibFolder.list((dir, name) -> name.endsWith(".jar"));
      return asList(libraries);
    }
    return new ArrayList<>();
  }

  protected void listPlugins(String artifactType, DeployableArtifactDescriptor descriptor) {
    Set<ArtifactPluginDescriptor> plugins = descriptor.getPlugins();
    if (!plugins.isEmpty()) {
      doBody(artifactType + " plugins:");
      for (ArtifactPluginDescriptor plugin : plugins) {
        doBody(format(VALUE_FORMAT, format("%s : %s", plugin.getName(), plugin.getBundleDescriptor().getVersion())));
      }
    }
  }
}
