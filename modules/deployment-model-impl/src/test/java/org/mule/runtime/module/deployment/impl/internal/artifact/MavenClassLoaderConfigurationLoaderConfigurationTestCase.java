/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.artifact;

import static com.google.common.io.Files.createTempDir;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;
import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static org.mule.tck.MuleTestUtils.testWithSystemProperties;
import static org.mule.test.allure.AllureConstants.ClassloadingIsolationFeature.CLASSLOADING_ISOLATION;
import static org.mule.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.CLASSLOADER_CONFIGURATION;
import static org.mule.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.CLASSLOADER_CONFIGURATION_LOADER;

import org.mule.runtime.globalconfig.api.GlobalConfigLoader;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Feature;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(CLASSLOADING_ISOLATION)
@Stories({@Story(CLASSLOADER_CONFIGURATION_LOADER), @Story(CLASSLOADER_CONFIGURATION)})
public class MavenClassLoaderConfigurationLoaderConfigurationTestCase extends MavenClassLoaderConfigurationLoaderTestCase {

  public static final String MULE_RUNTIME_CONFIG_MAVEN_REPOSITORY_LOCATION = "muleRuntimeConfig.maven.repositoryLocation";

  private File artifactFile;

  @Rule
  public SystemProperty repositoryLocation = new SystemProperty(MULE_RUNTIME_CONFIG_MAVEN_REPOSITORY_LOCATION,
                                                                createTempDir().getAbsolutePath());
  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void before() throws URISyntaxException {
    artifactFile = getApplicationFolder("apps/single-dependency");
  }

  @Test
  public void noMavenConfiguration() throws Exception {
    Map<String, String> properties = getMuleFreeSystemProperties();
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(instanceOf(DependencyResolutionException.class));
    testWithSystemProperties(properties, () -> {
      GlobalConfigLoader.reset(); // Change local repository path
      mavenClassLoaderConfigurationLoader.load(artifactFile, emptyMap(), APP);
    });
  }

  @Test
  public void changeMavenConfiguration() throws Exception {
    Map<String, String> properties = getMuleFreeSystemProperties();
    properties.put(repositoryLocation.getName(), repositoryLocation.getValue());
    try {
      testWithSystemProperties(properties, () -> {
        GlobalConfigLoader.reset(); // Change local repository path
        mavenClassLoaderConfigurationLoader.load(artifactFile, emptyMap(), APP);
      });
      fail();
    } catch (Exception e) {
      // It is should fail
    }
    properties.put("muleRuntimeConfig.maven.repositories.mavenCentral.url", "https://repo.maven.apache.org/maven2/");

    Map<String, Object> attributes =
        ImmutableMap.of(org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor.class.getName(),
                        new org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor.Builder()
                            .setGroupId("groupId")
                            .setArtifactId("artifactId")
                            .setVersion("1.0.0")
                            .setType("jar")
                            .setClassifier("mule-application")
                            .build());

    try {
      testWithSystemProperties(properties, () -> range(1, 10).parallel().forEach(
                                                                                 number -> {
                                                                                   GlobalConfigLoader.reset();
                                                                                   try {
                                                                                     assertThat(mavenClassLoaderConfigurationLoader
                                                                                         .load(artifactFile, attributes, APP)
                                                                                         .getDependencies(),
                                                                                                hasItem(hasProperty("descriptor",
                                                                                                                    (hasProperty("artifactId",
                                                                                                                                 equalTo("commons-collections"))))));
                                                                                   } catch (Exception e) {
                                                                                     throw new RuntimeException(e);
                                                                                   }
                                                                                 }));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private Map<String, String> getMuleFreeSystemProperties() {
    // Find any System property for muleRuntimeConfig from previous executions...
    final List<String> muleRuntimeConfig =
        System.getProperties().stringPropertyNames().stream()
            .filter(propertyName -> propertyName.startsWith("muleRuntimeConfig") && !propertyName.equals(
                                                                                                         MULE_RUNTIME_CONFIG_MAVEN_REPOSITORY_LOCATION))
            .collect(
                     toList());
    Map<String, String> properties = new HashMap<>();
    muleRuntimeConfig.forEach(property -> properties.put(property, null));
    return properties;
  }

}
