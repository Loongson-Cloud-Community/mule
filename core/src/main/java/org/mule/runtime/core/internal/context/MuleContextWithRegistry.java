/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.context;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.internal.registry.MuleRegistry;
import org.mule.runtime.core.internal.registry.Registry;

/**
 * Declares {@link MuleRegistry} handling methods for {@link MuleContext}.
 *
 * @since 4.0
 * @deprecated Use {@link org.mule.runtime.api.artifact.Registry} instead.
 */
// TODO MULE-13627 Remove downcasts to this interface
@Deprecated
public interface MuleContextWithRegistry extends MuleContext {

  /**
   * @return the {@link MuleRegistry}
   */
  MuleRegistry getRegistry();

  /**
   * Sets the {@link Registry} that this context will use.
   * <p>
   * If the {@code registry} is not a {@link MuleRegistry} it will be adapted into one.
   * <p>
   * Notice that if the context already has a registry, it will be replaced without taking any particular lifecycle activity over
   * it. That will be up to you.
   *
   * @param registry the new registry to use
   */
  void setRegistry(Registry registry);

}
