/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.registry;

import static java.util.Optional.ofNullable;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.runtime.core.privileged.registry.RegistrationException;

import java.util.Collection;
import java.util.Optional;

/**
 * Default implementation for {@link Registry}.
 *
 * @since 4.0
 */
public class DefaultRegistry implements Registry {

  private MuleContextWithRegistry muleContext;

  public DefaultRegistry(MuleContext muleContext) {
    this.muleContext = (MuleContextWithRegistry) muleContext;
  }

  @Override
  public <T> Optional<T> lookupByType(Class<T> objectType) {
    try {
      return ofNullable(muleContext.getRegistry().lookupObject(objectType));
    } catch (RegistrationException e) {
      throw new MuleRuntimeException(e);
    }
  }

  @Override
  public <T> Optional<T> lookupByName(String name) {
    return ofNullable(muleContext.getRegistry().lookupObject(name));
  }

  @Override
  public <T> Collection<T> lookupAllByType(Class<T> serviceType) {
    return muleContext.getRegistry().lookupObjects(serviceType);
  }
}
