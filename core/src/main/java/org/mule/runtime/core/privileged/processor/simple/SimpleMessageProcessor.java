/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.processor.simple;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;

/**
 * Base class for {@link Processor}s that change the event or message. Implementations will return a new instance of the event
 * with the updated data, since the {@link CoreEvent} and {@link Message} objects are immutable.
 *
 * @since 4.0
 */
public abstract class SimpleMessageProcessor extends AbstractComponent
    implements Processor, MuleContextAware, Initialisable {

  protected MuleContext muleContext;

  @Override
  public void setMuleContext(MuleContext context) {
    this.muleContext = context;
  }

}
