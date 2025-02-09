/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.registry;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;

import java.util.Collection;

/**
 * Adds lookup/register/unregister methods for Mule-specific entities to the standard Registry interface.
 */
public interface MuleRegistry extends Registry {

  /**
   * Pass this flag as metadata of the {@link Registry#registerObject(String, Object, Object)} method to have lifecycle method
   * calls on the registered objects omitted. Unless extending Mule, one will probably never have a use for this.
   *
   * @see Registry#registerObject(String, Object, Object)
   */
  // TODO W-10781591 Remove this
  int LIFECYCLE_BYPASS_FLAG = 0x01;

  /**
   * Determines whether Inject processors should get executed on an object added to the registry Inject processors are responsible
   * for processing inject interfaces such as {@link org.mule.runtime.core.api.context.MuleContextAware}
   */
  // TODO W-10781591 Remove this
  int INJECT_PROCESSORS_BYPASS_FLAG = 0x02;

  /**
   * Determines whether pre-init processors should get executed on an object added to the registry. Pre init processors are
   * basically object processors that do not inject members into objects. These processors happen after the inject processors
   */
  // TODO W-10781591 Remove this
  int PRE_INIT_PROCESSORS_BYPASS_FLAG = 0x04;

  // /////////////////////////////////////////////////////////////////////////
  // Lookup methods - these should NOT create a new object, only return existing ones
  // /////////////////////////////////////////////////////////////////////////

  FlowConstruct lookupFlowConstruct(String name);

  Collection<FlowConstruct> lookupFlowConstructs();

  // /////////////////////////////////////////////////////////////////////////
  // Registration methods
  // /////////////////////////////////////////////////////////////////////////

  void registerFlowConstruct(FlowConstruct flowConstruct) throws MuleException;

  /**
   * Will execute any processors on an object and fire any lifecycle methods according to the current lifecycle without actually
   * registering the object in the registry. This is useful for prototype objects that are created per request and would clutter
   * the registry with single use objects. Not that this will only be applied to Mule registies. Thrid party registries such as
   * Guice support wiring, but you need to get a reference to the container/context to call the method. This is so that wiring
   * mechanisms dont trip over each other.
   *
   * @param object the object to process
   * @return the same object with any processors and lifecycle methods called
   * @throws MuleException if the registry fails to perform the lifecycle change or process object processors for the object.
   */
  Object applyProcessorsAndLifecycle(Object object) throws MuleException;

  /**
   * Will execute any processors on an object without actually registering the object in the registry. This is useful for
   * prototype objects that are created per request and would clutter the registry with single use objects. Not that this will
   * only be applied to Mule registries. Third party registries such as Guice support wiring, but you need to get a reference to
   * the container/context to call the method. This is so that wiring mechanisms dont trip over each other.
   *
   * @param object the object to process
   * @return the same object with any processors called
   * @throws MuleException if the registry fails to process object processors for the object.
   */
  Object applyProcessors(Object object) throws MuleException;

  /**
   * @return the {@link MuleContext} that owns this registry instance. Non null.
   */
  MuleContext getMuleContext();
}
