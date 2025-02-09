/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.privileged.dsl.processor;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;
import static org.mule.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder.newLazyProcessorChainBuilder;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.privileged.processor.MessageProcessorBuilder;
import org.mule.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import org.mule.runtime.core.privileged.processor.chain.MessageProcessorChain;
import org.mule.runtime.core.privileged.processor.chain.MessageProcessorChainBuilder;
import org.mule.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.namespace.QName;

@NoExtend
public class MessageProcessorChainFactoryBean extends AbstractComponentFactory<MessageProcessorChain>
    implements MuleContextAware {

  protected List processors;
  protected String name;
  protected MuleContext muleContext;

  @Inject
  protected ConfigurationComponentLocator locator;

  public void setMessageProcessors(List processors) {
    this.processors = processors;
  }

  @Override
  public Map<QName, Object> getAnnotations() {
    return emptyMap();
  }

  @Override
  public MessageProcessorChain doGetObject() throws Exception {
    MessageProcessorChainBuilder builder = getBuilderInstance();
    for (Object processor : processors) {
      if (processor instanceof Processor) {
        builder.chain((Processor) processor);
      } else if (processor instanceof MessageProcessorBuilder) {
        builder.chain((MessageProcessorBuilder) processor);
      } else {
        throw new IllegalArgumentException(format("MessageProcessorBuilder should only have MessageProcessor's or MessageProcessorBuilder's configured. Found a %s",
                                                  processor.getClass().getName()));
      }
    }
    return newLazyProcessorChainBuilder((DefaultMessageProcessorChainBuilder) builder,
                                        muleContext,
                                        () -> getProcessingStrategy(locator, this).orElse(null));
  }

  protected MessageProcessorChainBuilder getBuilderInstance() {
    DefaultMessageProcessorChainBuilder builder = new DefaultMessageProcessorChainBuilder();
    builder.setName("processor chain '" + name + "'");
    return builder;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setMuleContext(MuleContext context) {
    this.muleContext = context;
  }

}
