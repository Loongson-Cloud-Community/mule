/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.registry;

import org.mule.runtime.core.api.lifecycle.LifecycleState;
import org.mule.runtime.core.api.lifecycle.LifecycleStateAware;
import org.mule.runtime.core.privileged.registry.InjectProcessor;

/**
 * Injects the MuleContext object for objects stored in the {@link Registry} where the object registered implements
 * {@link org.mule.runtime.core.api.context.MuleContextAware}.
 *
 * @deprecated as of 3.7.0 since these are only used by {@link Registry} which is also deprecated. Use post processors for
 *             currently supported registries instead
 */
@Deprecated
public class LifecycleStateInjectorProcessor implements InjectProcessor {

  private LifecycleState state;

  public LifecycleStateInjectorProcessor(LifecycleState state) {
    this.state = state;
  }

  public Object process(Object object) {
    if (object instanceof LifecycleStateAware) {
      ((LifecycleStateAware) object).setLifecycleState(state);
    }
    return object;
  }
}
