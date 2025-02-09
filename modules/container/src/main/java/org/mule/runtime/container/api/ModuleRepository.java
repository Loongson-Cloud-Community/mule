/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.container.api;

import org.mule.runtime.container.internal.ClasspathModuleDiscoverer;
import org.mule.runtime.container.internal.CompositeModuleDiscoverer;
import org.mule.runtime.container.internal.DefaultModuleRepository;
import org.mule.runtime.container.internal.JreModuleDiscoverer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provides access to all Mule modules available on the container.
 */
public interface ModuleRepository {

  /**
   * Creates a ModuleRepository based on the modules available on the provided {@code classLoader}.
   * 
   * @param classLoader     where to look for modules.
   * @param temporaryFolder where to write the generated SPI mapping files.
   * @return a new {@link ModuleRepository} with the discovered information from the current runtime context.
   */
  public static ModuleRepository createModuleRepository(ClassLoader classLoader, File temporaryFolder) {
    return new DefaultModuleRepository(new CompositeModuleDiscoverer(new JreModuleDiscoverer(),
                                                                     new ClasspathModuleDiscoverer(classLoader,
                                                                                                   temporaryFolder)));
  }

  /**
   * Creates a ModuleRepository based on the modules available on the provided {@code classLoader}.
   * 
   * @param classLoader                   where to look for modules.
   * @param serviceInterfaceToServiceFile determines the SPI mapping file for the fully qualified interface service name.
   * @param fileToResource                obtains a {@link URL} from the SPI mapping file and the fully qualified interface
   *                                      service name
   * @return a new {@link ModuleRepository} with the discovered information from the current runtime context.
   * 
   * @since 4.5
   */
  public static ModuleRepository createModuleRepository(ClassLoader classLoader,
                                                        Function<String, File> serviceInterfaceToServiceFile,
                                                        BiFunction<String, File, URL> fileToResource) {
    return new DefaultModuleRepository(new CompositeModuleDiscoverer(new JreModuleDiscoverer(),
                                                                     new ClasspathModuleDiscoverer(classLoader,
                                                                                                   serviceInterfaceToServiceFile,
                                                                                                   fileToResource)));
  }

  /**
   * @return a non null list of {@link MuleModule}
   */
  List<MuleModule> getModules();
}
