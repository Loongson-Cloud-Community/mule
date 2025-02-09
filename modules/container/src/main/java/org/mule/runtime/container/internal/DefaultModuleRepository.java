/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.container.internal;

import static java.util.stream.Collectors.toList;
import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.runtime.container.api.ModuleRepository;
import org.mule.runtime.container.api.MuleModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Defines a {@link ModuleRepository} that uses a {@link ModuleDiscoverer} to find the available modules.
 */
public class DefaultModuleRepository implements ModuleRepository {

  protected static final Logger logger = LoggerFactory.getLogger(DefaultModuleRepository.class);

  private final ModuleDiscoverer moduleDiscoverer;
  private volatile List<MuleModule> modules;

  /**
   * Creates a new repository
   *
   * @param moduleDiscoverer used to discover available modules. Non null.
   */
  public DefaultModuleRepository(ModuleDiscoverer moduleDiscoverer) {
    checkArgument(moduleDiscoverer != null, "moduleDiscoverer cannot be null");

    this.moduleDiscoverer = moduleDiscoverer;
  }

  @Override
  public List<MuleModule> getModules() {
    if (modules == null) {
      synchronized (this) {
        if (modules == null) {
          modules = discoverModules();

          if (logger.isDebugEnabled()) {
            logger.debug("Found {} modules: {}", modules.size(), modules.stream().map(m -> m.getName()).collect(toList()));
          }
        }
      }
    }

    return modules;
  }

  protected List<MuleModule> discoverModules() {
    return moduleDiscoverer.discover();
  }
}
