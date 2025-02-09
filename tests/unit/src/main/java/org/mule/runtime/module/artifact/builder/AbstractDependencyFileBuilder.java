/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.builder;

import static org.mule.maven.pom.parser.api.model.BundleScope.COMPILE;
import static org.mule.maven.pom.parser.api.model.BundleScope.valueOf;
import static org.mule.maven.pom.parser.api.model.MavenModelBuilderProvider.discoverProvider;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.maven.pom.parser.api.model.MavenModelBuilder;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Base class for all kind of artifact that may exists in mule.
 * <p>
 * Provides all the support needed for maven.
 *
 * @param <T> the actual type of the builder.
 */
public abstract class AbstractDependencyFileBuilder<T extends AbstractDependencyFileBuilder<T>> {

  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  private final String artifactId;
  private final List<AbstractDependencyFileBuilder> dependencies = new ArrayList<>();
  private final List<AbstractDependencyFileBuilder> sharedLibraries = new ArrayList<>();
  private String groupId = "org.mule.test";
  private String version = "1.0.0";
  private final String type = "jar";
  private String classifier;
  private String scope;
  private File artifactPomFile;
  private File artifactPomPropertiesFile;
  private File tempFolder;

  /**
   * @param artifactId the maven artifact id
   */
  public AbstractDependencyFileBuilder(String artifactId) {
    checkArgument(artifactId != null, "artifact id cannot be null");
    this.artifactId = artifactId;
  }

  protected String getTempFolder() {
    if (tempFolder == null) {
      tempFolder = new File(JAVA_IO_TMPDIR, getArtifactId() + currentTimeMillis());
      tempFolder.deleteOnExit();
      if (tempFolder.exists()) {
        tempFolder.delete();
      }
      tempFolder.mkdir();
    }

    return tempFolder.getAbsolutePath();
  }

  /**
   * Sets the temporary folder to be used to create the artifact file.
   *
   * @param tempFolder temporary folder to use to create the artifact file.
   * @return the same builder instance
   */
  public T tempFolder(File tempFolder) {
    checkArgument(tempFolder != null, "tempFolder cannot be null");
    checkArgument(tempFolder.isDirectory(), "tempFolder must be a directory");
    this.tempFolder = tempFolder;
    return getThis();
  }

  public abstract File getArtifactFile();

  public File getArtifactPomFile() {
    if (artifactPomFile == null) {
      checkArgument(!isEmpty(artifactId), "Filename cannot be empty");

      final File tempFile = new File(getTempFolder(), artifactId + ".pom");
      tempFile.deleteOnExit();

      MavenModelBuilder model =
          discoverProvider().createMavenModelBuilder(getGroupId(), getArtifactId(), getVersion(), of("4.0.0"), empty());

      if (!sharedLibraries.isEmpty()) {
        createMuleMavenPlugin(model);
      }

      for (AbstractDependencyFileBuilder fileBuilderDependency : dependencies) {
        model.addDependency(fileBuilderDependency.getAsMavenDependency());
      }

      artifactPomFile = new File(tempFile.getAbsolutePath());
      model.updateArtifactPom(artifactPomFile.toPath());
    }
    return artifactPomFile;
  }

  public File getArtifactPomPropertiesFile() {
    if (artifactPomPropertiesFile == null) {
      checkArgument(!isEmpty(artifactId), "Filename cannot be empty");

      final File tempFile = new File(getTempFolder(), "pom.properties");
      tempFile.deleteOnExit();

      artifactPomPropertiesFile = new File(tempFile.getAbsolutePath());
      Properties pomProperties = new Properties();
      pomProperties.setProperty("groupId", getGroupId());
      pomProperties.setProperty("artifactId", getArtifactId());
      pomProperties.setProperty("version", getVersion());
      try (FileOutputStream fileOutputStream = new FileOutputStream(artifactPomPropertiesFile)) {
        pomProperties.store(fileOutputStream, null);
      } catch (IOException e) {
        throw new MuleRuntimeException(e);
      }
    }
    return artifactPomPropertiesFile;
  }

  private void createMuleMavenPlugin(MavenModelBuilder model) {
    dependencies.stream().filter(sharedLibraries::contains)
        .forEach(sharedLibrary -> {
          model.addSharedLibraryDependency(sharedLibrary.groupId, sharedLibrary.artifactId);
        });
  }

  public T dependingOn(AbstractDependencyFileBuilder dependencyFileBuilder) {
    dependencies.add(dependencyFileBuilder);
    return getThis();
  }

  /**
   * Adds a new dependency that will be visible to other plugins within the artifact.
   *
   * @param dependencyFileBuilder shared dependency.
   * @return the same builder instance
   */
  public T dependingOnSharedLibrary(AbstractDependencyFileBuilder dependencyFileBuilder) {
    dependencies.add(dependencyFileBuilder);
    sharedLibraries.add(dependencyFileBuilder);
    return getThis();
  }

  /**
   * @param groupId the maven group id
   * @return the same builder instance
   */
  public T withGroupId(String groupId) {
    this.groupId = groupId;
    return getThis();
  }

  /**
   * @param version the maven version
   * @return the same builder instnace
   */
  public T withVersion(String version) {
    this.version = version;
    return getThis();
  }

  /**
   * @param classifier the maven classifier
   * @return the same builder instance
   */
  public T withClassifier(String classifier) {
    this.classifier = classifier;
    return getThis();
  }

  /**
   * @param scope the maven scope
   * @return the same builder instance
   */
  public T withScope(String scope) {
    this.scope = scope;
    return getThis();
  }

  /**
   * @return maven group id
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @return maven artifact id
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * @return maven version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return maven type
   */
  public String getType() {
    return type;
  }

  /**
   * @return maven classifier
   */
  public String getClassifier() {
    return classifier;
  }

  /**
   * @return maven scope
   */
  public String getScope() {
    return scope;
  }

  /**
   * @return the path within the maven repository this artifact is located in.
   */
  public String getArtifactFileRepositoryPath() {
    return getGroupId().replace(".", File.separator) + File.separator
        + Paths
            .get(getArtifactId(), getVersion(),
                 getArtifactId() + "-" + getVersion() + (getClassifier() != null ? "-" + getClassifier() : "") + "." + type)
            .toString();
  }

  /**
   * @return the path of the folder within the maven repository where this artifact is located in.
   */
  public String getArtifactFileRepositoryFolderPath() {
    return getGroupId().replace(".", File.separator) + File.separator
        + Paths
            .get(getArtifactId(), getVersion())
            .toString();
  }

  /**
   * @return the path within the maven repository this artifact pom is located in.
   */
  public String getArtifactFilePomRepositoryPath() {
    return getGroupId().replace(".", File.separator) + File.separator
        + Paths.get(getArtifactId(), getVersion(), getArtifactId() + "-" + getVersion() + ".pom").toString();
  }

  /**
   * @return the path within the artifact file where the pom file is bundled
   */
  public String getArtifactFileBundledPomPartialUrl() {
    return "META-INF/maven/" + getGroupId() + "/" + getArtifactId() + "/pom.xml";
  }

  /**
   * @return the path within the artifact file where the pom properties file is bundled
   */
  public String getArtifactFileBundledPomPropertiesPartialUrl() {
    return "META-INF/maven/" + getGroupId() + "/" + getArtifactId() + "/pom.properties";
  }

  /**
   * Creates a {@link BundleDependency} object from this artifact with scope compile.
   *
   * @return a maven
   */
  public BundleDependency getAsMavenDependency() {
    BundleDescriptor bundleDescriptor = new BundleDescriptor.Builder()
        .setVersion(getVersion())
        .setGroupId(getGroupId())
        .setArtifactId(getArtifactId())
        .setClassifier(getClassifier())
        .setType(getType())
        .build();
    return new BundleDependency.Builder()
        .setBundleDescriptor(bundleDescriptor)
        .setScope(valueOf(ofNullable(getScope()).orElse(COMPILE.name())))
        .build();
  }

  /**
   * @return current instance. Used just to avoid compilation warnings.
   */
  protected abstract T getThis();

  /**
   * @return the collection of dependencies of this artifact.
   */
  public List<AbstractDependencyFileBuilder> getDependencies() {
    return dependencies;
  }

  /**
   * @return a collection of all the compile dependencies of this artifact including the transitive ones
   */
  public List<AbstractDependencyFileBuilder> getAllCompileDependencies() {
    Set<AbstractDependencyFileBuilder> allCompileDependencies = new HashSet<>();
    for (AbstractDependencyFileBuilder dependency : dependencies) {
      if (dependency.getAsMavenDependency().getScope().equals(COMPILE)) {
        allCompileDependencies.addAll(dependency.getAllCompileDependencies());
        allCompileDependencies.add(dependency);
      }
    }
    return new ArrayList<>(allCompileDependencies);
  }

  /**
   * @return a collection of all the dependencies of this artifact that must be present in the {@code classloader-model.json}
   *         file.
   */
  public List<AbstractDependencyFileBuilder> getAllClassLoaderModelDependencies() {
    Set<AbstractDependencyFileBuilder> allClassLoaderModelDependencies = new HashSet<>(dependencies);
    for (AbstractDependencyFileBuilder dependency : dependencies) {
      if (dependency.getClassifier() != null && dependency.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)) {
        allClassLoaderModelDependencies.addAll(getAllPluginDependencies(dependency));
      }
    }
    return new ArrayList<>(allClassLoaderModelDependencies);
  }

  /**
   * @return a collection of all the transitive dependencies of the given dependency that are plugins.
   * @param dependency
   */
  private List<AbstractDependencyFileBuilder> getAllPluginDependencies(AbstractDependencyFileBuilder dependency) {
    Set<AbstractDependencyFileBuilder> allPluginDependencies = new HashSet<>();
    dependency.getDependencies().forEach(td -> {
      AbstractDependencyFileBuilder transitiveDependency = (AbstractDependencyFileBuilder) td;
      if (transitiveDependency.getClassifier() != null && transitiveDependency.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)) {
        allPluginDependencies.add(transitiveDependency);
        allPluginDependencies.addAll(getAllPluginDependencies(transitiveDependency));
      }
    });
    return new ArrayList<>(allPluginDependencies);
  }

  protected boolean isShared(AbstractDependencyFileBuilder dependencyFileBuilder) {
    return this.sharedLibraries.contains(dependencyFileBuilder);
  }

}
