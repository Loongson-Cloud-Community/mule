/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.jpms.api;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

/**
 * No-op implementation of JpmsUtils to use when running on JVM 8.
 * 
 * @since 4.5
 */
public final class JpmsUtils {

  private JpmsUtils() {
    // Nothing to do
  }

  /**
   * Creates a basic classLoader containing the given {@code modulePathEntries} and with the given {@code parent}.
   * 
   * @param modulePathEntries the URLs from which to load classes and resources
   * @param parent            the parent class loader for delegation
   * @return a new classLoader.
   */
  public static ClassLoader createModuleLayerClassLoader(URL[] modulePathEntries, ClassLoader parent) {
    return new URLClassLoader(modulePathEntries, parent);
  }

  /**
   * Creates two classLoaders for the given {@code modulePathEntriesParent} and {@code modulePathEntriesChild} and with the given
   * {@code parent}.
   * 
   * @param modulePathEntriesParent the URLs from which to find the modules of the parent
   * @param modulePathEntriesChild  the URLs from which to find the modules of the child
   * @param parent                  the parent class loader for delegation
   * @return a new classLoader.
   */
  public static ClassLoader createModuleLayerClassLoader(URL[] modulePathEntriesParent, URL[] modulePathEntriesChild,
                                                         ClassLoader parent) {
    return new URLClassLoader(modulePathEntriesChild, new URLClassLoader(modulePathEntriesParent, parent));
  }

  public static void exploreJdkModules(Set<String> packages) {
    // nothing to do
  }

  public static void validateNoBootModuleLayerTweaking() {
    // nothing to do
  }

}
