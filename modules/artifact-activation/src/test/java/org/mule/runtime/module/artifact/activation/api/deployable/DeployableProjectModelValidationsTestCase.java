/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.artifact.activation.api.deployable;

import static org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor.MULE_PLUGIN_CLASSIFIER;
import static org.mule.test.allure.AllureConstants.ClassloadingIsolationFeature.CLASSLOADING_ISOLATION;
import static org.mule.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.ARTIFACT_DESCRIPTORS;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import static org.junit.rules.ExpectedException.none;

import org.mule.runtime.module.artifact.api.descriptor.BundleDependency;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(CLASSLOADING_ISOLATION)
@Story(ARTIFACT_DESCRIPTORS)
public class DeployableProjectModelValidationsTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expected = none();

  private BundleDescriptor appDescriptor;

  @Before
  public void setUp() {
    appDescriptor = new BundleDescriptor.Builder()
        .setGroupId("org.mule.sample")
        .setArtifactId("test-app")
        .setVersion("0.0.1")
        .setClassifier("mule-application")
        .build();
  }

  @Test
  public void sharedLibraryNotInProject() {
    List<BundleDependency> dependencies = new ArrayList<>();

    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-dep-a")
            .setVersion("0.0.1")
            .build())
        .build());

    Set<BundleDescriptor> sharedLibraries = new HashSet<>();
    sharedLibraries.add(new BundleDescriptor.Builder()
        .setGroupId("org.mule.sample")
        .setArtifactId("test-dep-b")
        .setVersion("0.0.1")
        .build());

    expected.expect(IllegalArgumentException.class);
    expected
        .expectMessage(" * Artifact 'org.mule.sample:test-dep-b' is declared as a sharedLibrary but is not a dependency of the project");
    new DeployableProjectModel(emptyList(), emptyList(),
                               appDescriptor,
                               () -> null,
                               new File("."),
                               dependencies,
                               emptyList(), sharedLibraries,
                               emptyMap());
  }

  @Test
  public void sharedLibraryInProject() {
    BundleDescriptor testDep = new BundleDescriptor.Builder()
        .setGroupId("org.mule.sample")
        .setArtifactId("test-dep-a")
        .setVersion("0.0.1")
        .build();

    List<BundleDependency> dependencies = new ArrayList<>();
    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(testDep)
        .build());

    Set<BundleDescriptor> sharedLibraries = new HashSet<>();
    sharedLibraries.add(testDep);

    new DeployableProjectModel(emptyList(), emptyList(),
                               appDescriptor,
                               () -> null,
                               new File("."),
                               dependencies,
                               emptyList(), sharedLibraries,
                               emptyMap());
  }

  @Test
  public void pluginForAdditionalDependenciesNotInProject() {
    List<BundleDependency> dependencies = new ArrayList<>();
    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-plugin-z")
            .setVersion("0.0.1")
            .setClassifier(MULE_PLUGIN_CLASSIFIER)
            .build())
        .build());

    Map<BundleDescriptor, List<BundleDependency>> additionalPluginDependencies = new HashMap<>();
    additionalPluginDependencies.put(new BundleDescriptor.Builder()
        .setGroupId("org.mule.sample")
        .setArtifactId("test-plugin-a")
        .setVersion("0.0.1")
        .setClassifier(MULE_PLUGIN_CLASSIFIER)
        .build(), asList(new BundleDependency.Builder()
            .setDescriptor(new BundleDescriptor.Builder()
                .setGroupId("org.mule.sample")
                .setArtifactId("test-dep-a")
                .setVersion("0.0.1")
                .build())
            .build()));

    expected.expect(IllegalArgumentException.class);
    expected
        .expectMessage(" * Mule Plugin 'org.mule.sample:test-plugin-a' is declared in additionalPluginDependencies but is not a dependency of the project");
    new DeployableProjectModel(emptyList(), emptyList(),
                               appDescriptor,
                               () -> null,
                               new File("."),
                               dependencies,
                               emptyList(), emptySet(),
                               additionalPluginDependencies);
  }

  @Test
  public void pluginForAdditionalDependenciesInProject() {
    BundleDescriptor testPlugin = new BundleDescriptor.Builder()
        .setGroupId("org.mule.sample")
        .setArtifactId("test-plugin-a")
        .setVersion("0.0.1")
        .setClassifier(MULE_PLUGIN_CLASSIFIER)
        .build();

    List<BundleDependency> dependencies = new ArrayList<>();
    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(testPlugin)
        .build());

    Map<BundleDescriptor, List<BundleDependency>> additionalPluginDependencies = new HashMap<>();
    additionalPluginDependencies.put(testPlugin, asList(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-dep-a")
            .setVersion("0.0.1")
            .build())
        .build()));

    new DeployableProjectModel(emptyList(), emptyList(),
                               appDescriptor,
                               () -> null,
                               new File("."),
                               dependencies,
                               emptyList(), emptySet(),
                               additionalPluginDependencies);
  }

  @Test
  // TODO W-11202204 review this
  @Ignore("W-11202204")
  public void conflictingPluginVersions() {
    List<BundleDependency> dependencies = new ArrayList<>();

    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-plugin-a")
            .setVersion("0.0.1")
            .setClassifier(MULE_PLUGIN_CLASSIFIER)
            .build())
        .build());
    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-plugin-a")
            .setVersion("1.1.1")
            .setClassifier(MULE_PLUGIN_CLASSIFIER)
            .build())
        .build());

    expected.expect(IllegalArgumentException.class);
    expected
        .expectMessage(" * Mule Plugin 'org.mule.sample:test-plugin-a' is depended upon in the project with incompatible versions ('0.0.1, 1.1.1') in the dependency graph.");
    new DeployableProjectModel(emptyList(), emptyList(),
                               appDescriptor,
                               () -> null,
                               new File("."),
                               dependencies,
                               emptyList(), emptySet(),
                               emptyMap());
  }

  @Test
  public void nonConflictingPluginVersions() {
    List<BundleDependency> dependencies = new ArrayList<>();

    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-plugin-1")
            .setVersion("0.0.1")
            .setClassifier(MULE_PLUGIN_CLASSIFIER)
            .build())
        .build());
    dependencies.add(new BundleDependency.Builder()
        .setDescriptor(new BundleDescriptor.Builder()
            .setGroupId("org.mule.sample")
            .setArtifactId("test-plugin-a")
            .setVersion("0.1.1")
            .setClassifier(MULE_PLUGIN_CLASSIFIER)
            .build())
        .build());

    new DeployableProjectModel(emptyList(), emptyList(),
                               appDescriptor,
                               () -> null,
                               new File("."),
                               dependencies,
                               emptyList(), emptySet(),
                               emptyMap());

  }

}
