/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.launcher.coreextension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.mule.runtime.container.api.MuleCoreExtension;
import org.mule.runtime.container.api.MuleCoreExtensionDependency;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.List;

import org.junit.Test;

@SmallTest
public class ReflectionMuleCoreExtensionDependencyDiscovererTestCase extends AbstractMuleTestCase {

  private ReflectionMuleCoreExtensionDependencyDiscoverer dependencyDiscoverer =
      new ReflectionMuleCoreExtensionDependencyDiscoverer();

  @Test
  public void resolvesEmptyDependencies() throws Exception {
    final List<LinkedMuleCoreExtensionDependency> dependencies = dependencyDiscoverer.findDependencies(new TestCoreExtension());
    assertThat(dependencies.size(), equalTo(0));
  }

  @Test
  public void resolvesSingleDependency() throws Exception {
    final List<LinkedMuleCoreExtensionDependency> dependencies =
        dependencyDiscoverer.findDependencies(new DependantTestCoreExtension());
    assertThat(dependencies.size(), equalTo(1));
    assertThat((Class<TestCoreExtension>) dependencies.get(0).getDependencyClass(), equalTo(TestCoreExtension.class));
    assertThat(dependencies.get(0).getDependantMethod().getName(), equalTo("setTestCoreExtension"));
  }

  public static class AbstractTestCoreExtension implements MuleCoreExtension {

    @Override
    public void dispose() {}

    @Override
    public void initialise() throws InitialisationException {}

    @Override
    public String getName() {
      return null;
    }

    @Override
    public void start() throws MuleException {}

    @Override
    public void stop() throws MuleException {}

    @Override
    public void setContainerClassLoader(ArtifactClassLoader containerClassLoader) {

    }
  }

  public static class TestCoreExtension extends AbstractTestCoreExtension {

  }

  public static class DependantTestCoreExtension extends AbstractTestCoreExtension {

    @MuleCoreExtensionDependency
    public void setTestCoreExtension(TestCoreExtension coreExtension) {

    }
  }
}
