/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.launcher.coreextension;

import org.mule.runtime.container.api.MuleCoreExtension;
import org.mule.runtime.container.api.MuleCoreExtensionDependency;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Discovers dependencies between {@link MuleCoreExtension} instances looking for methods annotated with
 * {@link MuleCoreExtensionDependency}
 */
public class ReflectionMuleCoreExtensionDependencyDiscoverer implements MuleCoreExtensionDependencyDiscoverer {

  @Override
  public List<LinkedMuleCoreExtensionDependency> findDependencies(MuleCoreExtension coreExtension) {
    List<LinkedMuleCoreExtensionDependency> result = new LinkedList<LinkedMuleCoreExtensionDependency>();

    final Method[] methods = coreExtension.getClass().getMethods();

    for (Method method : methods) {
      if (method.getAnnotation(MuleCoreExtensionDependency.class) != null) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
          if (MuleCoreExtension.class.isAssignableFrom(parameterTypes[0])) {
            final LinkedMuleCoreExtensionDependency linkedMuleCoreExtensionDependency =
                new LinkedMuleCoreExtensionDependency((Class<? extends MuleCoreExtension>) parameterTypes[0], method);
            result.add(linkedMuleCoreExtensionDependency);
          }
        }
      }
    }

    return result;
  }
}
