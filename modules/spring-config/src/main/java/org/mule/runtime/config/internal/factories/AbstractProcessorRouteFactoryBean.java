/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.factories;

import static org.mule.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;
import static org.mule.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder.newLazyProcessorChainBuilder;

import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.privileged.processor.MessageProcessorBuilder;
import org.mule.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import org.mule.runtime.core.privileged.processor.chain.MessageProcessorChain;
import org.mule.runtime.dsl.api.component.AbstractComponentFactory;
import org.mule.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;

import javax.inject.Inject;

public abstract class AbstractProcessorRouteFactoryBean<T> extends AbstractComponentFactory<T> {

  @Inject
  private MuleContext muleContext;

  @Inject
  protected ConfigurationComponentLocator locator;

  @Inject
  protected ComponentTracerFactory componentTracerFactory;

  private List<Processor> messageProcessors;

  public void setMessageProcessors(List<Processor> messageProcessors) {
    this.messageProcessors = messageProcessors;
  }

  @Override
  public T doGetObject() throws Exception {
    final DefaultMessageProcessorChainBuilder builder = new DefaultMessageProcessorChainBuilder();
    for (Object processor : messageProcessors) {
      if (processor instanceof Processor) {
        builder.chain((Processor) processor);
      } else if (processor instanceof MessageProcessorBuilder) {
        builder.chain((MessageProcessorBuilder) processor);
      } else {
        throw new IllegalArgumentException("MessageProcessorBuilder should only have MessageProcessors or MessageProcessorBuilders configured");
      }
    }
    MessageProcessorChain chain = newLazyProcessorChainBuilder(builder, muleContext,
                                                               () -> getProcessingStrategy(locator, this).orElse(null));
    return getProcessorRoute(chain);
  }

  protected abstract T getProcessorRoute(MessageProcessorChain chain);

}
