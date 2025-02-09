/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.lifecycle.phases;

import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.service.Service;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.component.Component;
import org.mule.runtime.core.api.config.Config;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.core.api.processor.InterceptingMessageProcessor;
import org.mule.runtime.core.api.util.queue.QueueManager;
import org.mule.runtime.core.internal.exception.GlobalErrorHandler;
import org.mule.runtime.core.internal.registry.Registry;
import org.mule.runtime.core.privileged.routing.OutboundRouter;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The Stop phase for the Management context LifecycleManager. Calling {@link MuleContext#stop()} with initiate this phase via the
 * {@link org.mule.runtime.core.api.lifecycle.LifecycleManager}.
 *
 *
 * The MuleContextDisposePhase defines the lifecycle behaviour when the Mule context is stopped. The MuleContext is associated
 * with one or more registries that inherit the lifecycle of the MuleContext.
 *
 * This phase is responsible for disposing objects. Any object that implements {@link Stoppable} will have its
 * {@link Stoppable#stop()} ()} method called. Objects are initialised in the order based on type:
 * {@link org.mule.runtime.core.api.construct.FlowConstruct} followed by any other object that implements {@link Stoppable}.
 *
 * @see org.mule.runtime.core.api.MuleContext
 * @see org.mule.runtime.core.api.lifecycle.LifecycleManager
 * @see Stoppable
 *
 * @since 3.0
 */
public class MuleContextStopPhase extends DefaultLifecyclePhase {

  public MuleContextStopPhase() {
    this(new Class[] {
        Registry.class,
        MuleContext.class,
        InterceptingMessageProcessor.class,
        Component.class,
        OutboundRouter.class,
        Service.class
    });
  }

  public MuleContextStopPhase(Class<?>[] ignoredObjects) {
    super(Stoppable.PHASE_NAME, LifecycleUtils::stopIfNeeded);

    setOrderedLifecycleTypes(new Class<?>[] {
        FlowConstruct.class,
        ConfigurationProvider.class,
        Config.class,
        QueueManager.class,
        Stoppable.class
    });

    ignoredObjects = ArrayUtils.add(ignoredObjects, GlobalErrorHandler.class);

    setIgnoredObjectTypes(ignoredObjects);
    // Yuo can initialise and stop
    registerSupportedPhase(Initialisable.PHASE_NAME);
    // Stop/Start/Stop
    registerSupportedPhase(Startable.PHASE_NAME);
  }
}
