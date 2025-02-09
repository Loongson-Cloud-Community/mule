/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.factories;

import static org.mule.runtime.core.privileged.processor.MessageProcessors.processToApply;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.processor.AnnotatedProcessor;

/**
 * Defines a common base class for flow-ref processors.
 *
 * @Since 4.3.0
 */
public abstract class FlowRefMessageProcessor extends AbstractComponent
    implements AnnotatedProcessor, Startable, Stoppable, Disposable {

  private final FlowRefFactoryBean owner;

  protected FlowRefMessageProcessor(FlowRefFactoryBean owner) {
    this.owner = owner;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws MuleException {
    return processToApply(event, this);
  }

  @Override
  public void start() throws MuleException {
    this.doStart();
  }

  @Override
  public ComponentLocation getLocation() {
    return owner.getLocation();
  }

  public abstract void doStart() throws MuleException;

  public FlowRefFactoryBean getOwner() {
    return owner;
  }
}
