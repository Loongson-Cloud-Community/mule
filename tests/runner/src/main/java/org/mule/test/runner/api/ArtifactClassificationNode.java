/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.runner.api;

import java.net.URL;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;

/**
 * Defines the result of the classification process for an artifact. It contains a {@link List} of {@link URL}s that should have
 * the plugin {@link ArtifactClassLoaderHolder} plus a {@link List} of {@link Class}es to be exported in addition to the packages
 * exported by the artifact, in order to run the test.
 * <p/>
 * It also has dependencies references and nested ones. It represents the graph of plugin dependencies also by the list of
 * children nodes.
 *
 * @since 4.0
 */
public class ArtifactClassificationNode {

  private List<URL> urls;
  private final Artifact artifact;
  private final List<Class> exportClasses;
  private final List<ArtifactClassificationNode> artifactDependencies;

  /**
   * Creates an instance of the classification.
   *
   * @param artifact             the classified {@link Artifact}
   * @param urls                 list of {@link URL}s that would be used to create the {@link java.net.URLClassLoader}
   * @param exportClasses        list of {@link Class}es that would be used for exporting as extra classes to the plugin
   * @param artifactDependencies list of {@link ArtifactClassificationNode} plugin dependencies references for this artifact
   *                             classified
   */
  public ArtifactClassificationNode(Artifact artifact, List<URL> urls, List<Class> exportClasses,
                                    List<ArtifactClassificationNode> artifactDependencies) {
    this.artifact = artifact;
    this.urls = urls;
    this.exportClasses = exportClasses;
    this.artifactDependencies = artifactDependencies;
  }

  public List<URL> getUrls() {
    return urls;
  }

  public Artifact getArtifact() {
    return artifact;
  }

  public List<Class> getExportClasses() {
    return exportClasses;
  }

  public List<ArtifactClassificationNode> getArtifactDependencies() {
    return artifactDependencies;
  }

  public void setUrls(List<URL> urls) {
    this.urls = urls;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArtifactClassificationNode that = (ArtifactClassificationNode) o;

    return artifact.equals(that.artifact);
  }

  @Override
  public int hashCode() {
    return artifact.hashCode();
  }

  @Override
  public String toString() {
    return artifact.toString();
  }
}
