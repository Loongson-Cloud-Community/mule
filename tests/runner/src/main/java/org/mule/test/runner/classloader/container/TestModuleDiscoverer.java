/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.runner.classloader.container;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import org.mule.runtime.container.api.MuleModule;
import org.mule.runtime.container.internal.DefaultModuleRepository;
import org.mule.runtime.container.internal.ModuleDiscoverer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link ModuleDiscoverer} that enables to change discovered modules to enable testing privileged API scenarios.
 *
 * @since 4.5
 */
public class TestModuleDiscoverer implements ModuleDiscoverer {

  private final Set<String> privilegedArtifactIds;
  private ModuleDiscoverer delegateModuleDiscoverer;

  /**
   * Creates a module discoverer
   *
   * @param privilegedArtifactIds identifiers of the artifacts that will be conceded privileged API access. Non null
   */
  public TestModuleDiscoverer(Set<String> privilegedArtifactIds) {
    checkArgument(privilegedArtifactIds != null, "privilegedArtifactIds cannot be null");

    this.privilegedArtifactIds = privilegedArtifactIds;
    this.delegateModuleDiscoverer = new TestContainerModuleDiscoverer(this.getClass().getClassLoader());
  }

  /**
   * Creates a module discoverer
   *
   * @param privilegedArtifactIds identifiers of the artifacts that will be conceded privileged API access. Non null
   */
  public TestModuleDiscoverer(Set<String> privilegedArtifactIds, ModuleDiscoverer moduleDiscoverer) {
    checkArgument(privilegedArtifactIds != null, "privilegedArtifactIds cannot be null");
    checkArgument(moduleDiscoverer != null, "moduleDiscoverer cannot be null");

    this.privilegedArtifactIds = privilegedArtifactIds;
    this.delegateModuleDiscoverer = moduleDiscoverer;
  }

  @Override
  public List<MuleModule> discover() {
    DefaultModuleRepository containerModuleDiscoverer =
        new DefaultModuleRepository(delegateModuleDiscoverer);

    List<MuleModule> discoveredModules = containerModuleDiscoverer.getModules();
    List<MuleModule> updateModules = new ArrayList<>(discoveredModules.size());
    for (MuleModule discoveredModule : discoveredModules) {
      if (!discoveredModule.getPrivilegedExportedPackages().isEmpty()) {
        discoveredModule = updateModuleForTests(discoveredModule);
      }
      updateModules.add(discoveredModule);
    }

    return updateModules;
  }

  private MuleModule updateModuleForTests(MuleModule discoveredModule) {
    Set<String> privilegedArtifacts = new HashSet<>(discoveredModule.getPrivilegedArtifacts());
    privilegedArtifacts.addAll(privilegedArtifactIds);

    return new MuleModule(discoveredModule.getName(), discoveredModule.getExportedPackages(),
                          discoveredModule.getExportedPaths(), discoveredModule.getPrivilegedExportedPackages(),
                          privilegedArtifacts, discoveredModule.getExportedServices());
  }
}
