/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.lifecycle.phases;

import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.service.Service;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.component.Component;
import org.mule.runtime.core.api.config.Config;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.exception.FlowExceptionHandler;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.core.api.processor.InterceptingMessageProcessor;
import org.mule.runtime.core.api.util.queue.QueueManager;
import org.mule.runtime.core.internal.registry.Registry;
import org.mule.runtime.core.privileged.routing.OutboundRouter;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;

/**
 * The Start phase for the MuleContext. Calling {@link MuleContext#start()} will initiate this phase via the
 * {@link org.mule.runtime.core.api.lifecycle.LifecycleManager}.
 * <p/>
 * The MuleContextStartPhase defines the lifecycle behaviour when the Mule context is started. The MuleContext is associated with
 * one or more registries that inherit the lifecycle of the MuleContext.
 * <p/>
 * This phase is responsible for starting objects. Any object that implements {@link Startable} will have its
 * {@link Startable#start()} method called. Objects are initialised in the order based on type:
 * {@link org.mule.runtime.core.api.construct.FlowConstruct}, followed by any other object that implements {@link Startable}.
 *
 * @see org.mule.runtime.core.api.MuleContext
 * @see org.mule.runtime.core.api.lifecycle.LifecycleManager
 * @see Startable
 * @since 3.0
 */
public class MuleContextStartPhase extends DefaultLifecyclePhase {

  public MuleContextStartPhase() {
    this(new Class[] {
        Registry.class,
        MuleContext.class,
        InterceptingMessageProcessor.class,
        Component.class,
        OutboundRouter.class,
        MuleContext.class,
        Service.class,
        FlowExceptionHandler.class
    });
  }

  public MuleContextStartPhase(Class<?>[] ignoredObjects) {
    super(Startable.PHASE_NAME, LifecycleUtils::startIfNeeded);

    setIgnoredObjectTypes(ignoredObjects);
    setOrderedLifecycleTypes(new Class<?>[] {
        QueueManager.class,
        ObjectStore.class,
        ConfigurationProvider.class,
        Config.class,
        FlowConstruct.class,
        Startable.class
    });

    registerSupportedPhase(Initialisable.PHASE_NAME);
    // Start/Stop/Start
    registerSupportedPhase(Stoppable.PHASE_NAME);
  }
}
