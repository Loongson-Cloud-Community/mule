/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.functional.api.component;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.source.MessageSource;

import java.util.ArrayList;
import java.util.List;


public class LifecycleTrackerSource extends AbstractComponent implements Lifecycle, MuleContextAware, MessageSource {

  private static List<LifecycleTrackerSource> sources = new ArrayList<>();

  private final List<String> tracker = new ArrayList<>();
  private MuleContext muleContext;

  private Processor listener;

  public LifecycleTrackerSource() {
    sources.add(this);
  }

  public static List<LifecycleTrackerSource> getSources() {
    return sources;
  }

  public static void clearSources() {
    sources.clear();
  }

  @Override
  public void setListener(Processor listener) {
    this.listener = listener;
  }

  public List<String> getTracker() {
    return tracker;
  }

  public void setProperty(final String value) {
    getTracker().add("setProperty");
  }

  @Override
  public void setMuleContext(final MuleContext context) {
    if (muleContext == null) {
      getTracker().add("setMuleContext");
      this.muleContext = context;
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    getTracker().add("initialise");
  }

  @Override
  public void start() throws MuleException {
    getTracker().add("start");
  }

  @Override
  public void stop() throws MuleException {
    getTracker().add("stop");
  }

  @Override
  public void dispose() {
    getTracker().add("dispose");
  }
}
