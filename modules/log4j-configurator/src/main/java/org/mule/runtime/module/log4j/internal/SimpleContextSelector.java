/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.log4j.internal;

import static java.util.Arrays.asList;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.selector.ContextSelector;

/**
 * A simple {@link ContextSelector} which always returns the same {@link MuleLoggerContext} created through a
 * {@link MuleLog4jContextFactory}.
 * <p>
 * Log separation will always be disabled on the returned context.
 *
 * @since 4.5
 */
public class SimpleContextSelector implements ContextSelector {

  private final MuleLoggerContextFactory loggerContextFactory = new MuleLoggerContextFactory();

  private LoggerContext context;

  public SimpleContextSelector() {
    this.context = loggerContextFactory.build(getClass().getClassLoader(), this, false);
  }

  @Override
  public LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext) {
    return context;
  }

  @Override
  public LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext, URI configLocation) {
    return context;
  }

  @Override
  public List<LoggerContext> getLoggerContexts() {
    return asList(context);
  }

  @Override
  public void removeContext(LoggerContext context) {

  }
}
