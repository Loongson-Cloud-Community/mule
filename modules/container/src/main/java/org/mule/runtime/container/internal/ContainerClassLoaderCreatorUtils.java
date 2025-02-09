/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.container.internal;

import static org.mule.runtime.api.util.MuleSystemProperties.MULE_ALLOW_JRE_EXTENSION;
import static org.mule.runtime.api.util.MuleSystemProperties.MULE_JRE_EXTENSION_PACKAGES;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Arrays.stream;

import static com.google.common.collect.ImmutableSet.of;

import org.mule.runtime.container.api.MuleModule;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.classloader.LookupStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to aid in the creation of a container class loader.
 *
 * @since 4.5
 */
public class ContainerClassLoaderCreatorUtils {

  private static final String DEFAULT_JRE_EXTENSION_PACKAGES = "javax.,org.w3c.dom,org.omg.,org.xml.sax,org.ietf.jgss";
  private static final String MULE_SDK_API_PACKAGE = "org.mule.sdk.api";
  private static final String MULE_SDK_COMPATIBILITY_API_PACKAGE = "org.mule.sdk.compatibility.api";
  private static final boolean ALLOW_JRE_EXTENSION = parseBoolean(getProperty(MULE_ALLOW_JRE_EXTENSION, "true"));
  private static final String[] JRE_EXTENDABLE_PACKAGES =
      getProperty(MULE_JRE_EXTENSION_PACKAGES, DEFAULT_JRE_EXTENSION_PACKAGES).split(",");

  // TODO(pablo.kraan): MULE-9524: Add a way to configure system and boot packages used on class loading lookup
  /**
   * System packages define all the prefixes that must be loaded only from the container class loader, but then are filtered
   * depending on what is part of the exposed API.
   */
  public static final Set<String> SYSTEM_PACKAGES = of("org.mule.runtime", "com.mulesoft.mule.runtime");

  /**
   * Creates the container lookup policy to be used by child class loaders.
   *
   * @param parentClassLoader class loader used as parent of the container's. It's the classLoader that will load Mule classes.
   * @param muleModules       list of modules that would be used to register in the filter based of the class loader.
   * @return a non-null {@link ClassLoaderLookupPolicy} that contains the lookup policies for boot and system packages, plus
   *         exported packages by the given list of {@link MuleModule}s.
   */
  public static ClassLoaderLookupPolicy getLookupPolicy(ClassLoader parentClassLoader, List<MuleModule> muleModules,
                                                        Set<String> bootPackages) {
    final Set<String> parentOnlyPackages = new HashSet<>(bootPackages);
    parentOnlyPackages.addAll(SYSTEM_PACKAGES);
    final Map<String, LookupStrategy> lookupStrategies = buildClassLoaderLookupStrategy(parentClassLoader, muleModules);
    return new MuleClassLoaderLookupPolicy(lookupStrategies, parentOnlyPackages);
  }


  /**
   * Creates a {@link Map} for the packages exported on the container.
   *
   * @param containerClassLoader class loader containing container's classes. Non-null.
   * @param modules              to be used for collecting the exported packages. Non-null
   * @return a {@link Map} for the packages exported on the container
   */
  private static Map<String, LookupStrategy> buildClassLoaderLookupStrategy(ClassLoader containerClassLoader,
                                                                            List<MuleModule> modules) {
    checkArgument(containerClassLoader != null, "containerClassLoader cannot be null");
    checkArgument(modules != null, "modules cannot be null");

    ContainerOnlyLookupStrategy containerOnlyLookupStrategy = new ContainerOnlyLookupStrategy(containerClassLoader);

    final Map<String, LookupStrategy> result = new HashMap<>();
    for (MuleModule muleModule : modules) {
      for (String exportedPackage : muleModule.getExportedPackages()) {
        LookupStrategy specialLookupStrategy = getSpecialLookupStrategy(exportedPackage);
        result.put(exportedPackage, specialLookupStrategy == null ? containerOnlyLookupStrategy : specialLookupStrategy);
      }
    }

    return result;
  }

  /**
   * @param exportedPackage name of the package
   * @return the {@link LookupStrategy} if the one to use for the exportedPackage is other than a
   *         {@link ContainerOnlyLookupStrategy}, or null otherwise.
   */
  private static LookupStrategy getSpecialLookupStrategy(String exportedPackage) {
    // If an extension uses a class provided by the mule-sdk-api artifacts, the container classloader should use
    // the class with which the extension was compiled only if the class is not present in the distribution.
    if (exportedPackage.startsWith(MULE_SDK_API_PACKAGE) || exportedPackage.startsWith(MULE_SDK_COMPATIBILITY_API_PACKAGE)) {
      return PARENT_FIRST;
    }
    // Let artifacts extend non "java." JRE packages
    if (ALLOW_JRE_EXTENSION && stream(JRE_EXTENDABLE_PACKAGES).anyMatch(exportedPackage::startsWith)) {
      return PARENT_FIRST;
    }
    return null;
  }
}
