/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.functional.api.component;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.source.MessageSource;

/**
 * Test source that provides access to the {@link Processor} set by the owner {@link org.mule.runtime.core.api.construct.Flow}.
 *
 * @since 4.0
 */
public class SkeletonSource extends AbstractComponent implements MessageSource, Startable {

  private Processor listener;
  private volatile boolean started = false;

  @Override
  public void setListener(Processor listener) {
    this.listener = listener;
  }

  public Processor getListener() {
    return listener;
  }

  @Override
  public synchronized void start() throws MuleException {
    started = true;
  }

  public synchronized boolean isStarted() {
    return started;
  }
}
