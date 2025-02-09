/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.runner;

import static org.mule.test.runner.utils.AnnotationUtils.findConfiguredClass;
import static org.mule.test.runner.utils.AnnotationUtils.getAnnotationAttributeFrom;
import static org.mule.test.runner.utils.AnnotationUtils.getAnnotationAttributeFromHierarchy;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.mule.test.runner.utils.AnnotationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Configuration for {@link ArtifactClassLoaderRunner}.
 *
 * @since 4.0
 */
public class RunnerConfiguration {

  private static final String PROVIDED_EXCLUSIONS = "providedExclusions";
  private static final String TEST_EXCLUSIONS = "testExclusions";
  private static final String TEST_INCLUSIONS = "testInclusions";
  private static final String EXPORT_PLUGIN_CLASSES = "exportPluginClasses";
  private static final String SHARED_RUNTIME_LIBS = "applicationSharedRuntimeLibs";
  private static final String APPLICATION_RUNTIME_LIBS = "applicationRuntimeLibs";
  private static final String TEST_RUNNER_EXPORTED_RUNTIME_LIBS = "testRunnerExportedRuntimeLibs";
  private static final String EXTRA_PRIVILEGED_ARTIFACTS = "extraPrivilegedArtifacts";
  public static final String TEST_RUNNER_ARTIFACT_ID = "org.mule.tests.plugin:mule-tests-runner-plugin";
  public static final String TEST_UNIT_ARTIFACT_ID = "org.mule.tests:mule-tests-unit";

  private Set<String> providedExclusions;
  private Set<String> testExclusions;
  private Set<String> testInclusions;
  private Set<Class> exportPluginClasses;
  private Set<String> sharedApplicationRuntimeLibs;
  private Set<String> applicationRuntimeLibs;
  private Set<String> testRunnerExportedRuntimeLibs;
  private Set<String> extraPrivilegedArtifacts;
  private Map<String, String> systemProperties;

  private final String loadedFromTestClass;

  private RunnerConfiguration(Class loadedFromTestClass) {
    this.loadedFromTestClass = loadedFromTestClass.getName();
  }

  public Set<Class> getExportPluginClasses() {
    return exportPluginClasses;
  }

  public Set<String> getProvidedExclusions() {
    return providedExclusions;
  }

  public Set<String> getSharedApplicationRuntimeLibs() {
    return sharedApplicationRuntimeLibs;
  }

  public Set<String> getApplicationRuntimeLibs() {
    return applicationRuntimeLibs;
  }

  public Set<String> getTestRunnerExportedRuntimeLibs() {
    return testRunnerExportedRuntimeLibs;
  }

  public Set<String> getTestExclusions() {
    return testExclusions;
  }

  public Set<String> getTestInclusions() {
    return testInclusions;
  }

  public Set<String> getExtraPrivilegedArtifacts() {
    return extraPrivilegedArtifacts;
  }

  public Map<String, String> getSystemProperties() {
    return systemProperties;
  }

  /**
   * Creates an instance of the the configuration by reading the class annotated with {@link ArtifactClassLoaderRunnerConfig}.
   * <p/>
   * Configuration is created by searching in the class hierarchy for {@code testClass} which classes are annotated with
   * {@link ArtifactClassLoaderRunnerConfig} and creating a new configuration for the test. Some configuration attributes, as
   * {@value PROVIDED_EXCLUSIONS}, {@value TEST_EXCLUSIONS} and {@value TEST_INCLUSIONS}, are the result of collecting the same
   * attribute from all the configured classes/interfaces in the test class's hierarchy.
   * <p/>
   * The rest of the attributes, {@value EXPORT_PLUGIN_CLASSES}, {@value SHARED_RUNTIME_LIBS} and
   * {@value EXTRA_PRIVILEGED_ARTIFACTS}, are taken from the first class in the hierarchy that is configured.
   *
   * @param testClass Test {@link Class} annotated
   * @return a {@link RunnerConfiguration}
   * @see AnnotationUtils#findConfiguredClass(java.lang.Class)
   */
  public static RunnerConfiguration readConfiguration(Class testClass) {
    RunnerConfiguration runnerConfiguration = new RunnerConfiguration(testClass);

    runnerConfiguration.providedExclusions = new HashSet<>(readAttributeFromHierarchy(PROVIDED_EXCLUSIONS, testClass));
    runnerConfiguration.testExclusions = new HashSet<>(readAttributeFromHierarchy(TEST_EXCLUSIONS, testClass));
    runnerConfiguration.testInclusions = new HashSet<>(readAttributeFromHierarchy(TEST_INCLUSIONS, testClass));

    Class configuredClass = findConfiguredClass(testClass);
    runnerConfiguration.exportPluginClasses = new HashSet<>(readAttributeFromClass(EXPORT_PLUGIN_CLASSES, configuredClass));
    runnerConfiguration.sharedApplicationRuntimeLibs =
        new HashSet<>(readAttributeFromClass(SHARED_RUNTIME_LIBS, configuredClass));
    runnerConfiguration.applicationRuntimeLibs = new HashSet<>(readAttributeFromClass(APPLICATION_RUNTIME_LIBS, configuredClass));
    runnerConfiguration.extraPrivilegedArtifacts =
        new HashSet<>(readAttributeFromClass(EXTRA_PRIVILEGED_ARTIFACTS, configuredClass));
    runnerConfiguration.extraPrivilegedArtifacts.add(TEST_RUNNER_ARTIFACT_ID);
    runnerConfiguration.testRunnerExportedRuntimeLibs =
        new HashSet<>(readAttributeFromClass(TEST_RUNNER_EXPORTED_RUNTIME_LIBS, configuredClass));
    runnerConfiguration.testRunnerExportedRuntimeLibs.add(TEST_UNIT_ARTIFACT_ID);

    final List<RunnerConfigSystemProperty> systemProperties = readAttributeFromHierarchy("systemProperties", testClass);
    runnerConfiguration.systemProperties = systemProperties.stream()
        .collect(toMap(RunnerConfigSystemProperty::key, RunnerConfigSystemProperty::value));

    return runnerConfiguration;
  }

  /**
   * Reads the attribute from the klass annotated and does a flatMap with the list of values.
   *
   * @param name  attribute/method name of the annotation {@link ArtifactClassLoaderRunnerConfig} to be obtained
   * @param klass {@link Class} from where the annotated attribute will be read
   * @param <E>   generic type
   * @return {@link List} of values
   */
  private static <E> List<E> readAttributeFromHierarchy(String name, Class<?> klass) {
    List<E[]> valuesList =
        getAnnotationAttributeFromHierarchy(klass, ArtifactClassLoaderRunnerConfig.class,
                                            name);
    return valuesList.stream().flatMap(Arrays::stream).distinct().collect(toList());
  }

  private static <E> List<E> readAttributeFromClass(String name, Class<?> klass) {
    List<E[]> valuesList = singletonList(getAnnotationAttributeFrom(klass, ArtifactClassLoaderRunnerConfig.class, name));
    return valuesList.stream().flatMap(Arrays::stream).distinct().collect(toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RunnerConfiguration that = (RunnerConfiguration) o;

    if (!providedExclusions.equals(that.providedExclusions)) {
      return false;
    }
    if (!testExclusions.equals(that.testExclusions)) {
      return false;
    }
    if (!testInclusions.equals(that.testInclusions)) {
      return false;
    }
    if (!exportPluginClasses.equals(that.exportPluginClasses)) {
      return false;
    }
    if (!applicationRuntimeLibs.equals(that.applicationRuntimeLibs)) {
      return false;
    }
    if (!testRunnerExportedRuntimeLibs.equals(that.testRunnerExportedRuntimeLibs)) {
      return false;
    }
    return sharedApplicationRuntimeLibs.equals(that.sharedApplicationRuntimeLibs);
  }

  @Override
  public int hashCode() {
    int result = providedExclusions.hashCode();
    result = 31 * result + testExclusions.hashCode();
    result = 31 * result + testInclusions.hashCode();
    result = 31 * result + exportPluginClasses.hashCode();
    result = 31 * result + sharedApplicationRuntimeLibs.hashCode();
    result = 31 * result + applicationRuntimeLibs.hashCode();
    result = 31 * result + testRunnerExportedRuntimeLibs.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, SHORT_PREFIX_STYLE);
  }
}
