/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.internal.profiling;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.tracer.api.component.ComponentTracer;
import org.mule.runtime.tracer.api.component.ComponentTracerFactory;
import org.mule.runtime.tracer.api.span.InternalSpan;
import org.mule.runtime.tracer.api.span.validation.Assertion;
import org.mule.runtime.tracer.customization.api.InitialSpanInfoProvider;

import java.util.Optional;

/**
 * A dummy implementation of {@link InitialSpanInfoProvider}.
 *
 * @since 4.5.0
 */
public class DummyComponentTracerFactory implements ComponentTracerFactory {

  private static final DummyComponentTracerFactory INSTANCE = new DummyComponentTracerFactory();

  private static final DummyComponentTracer DUMMY_COMPONENT_TRACER_INSTANCE = new DummyComponentTracer();

  public static DummyComponentTracerFactory getDummyComponentTracerFactory() {
    return INSTANCE;
  }

  @Override
  public ComponentTracer<Event> fromComponent(Component component) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  @Override
  public ComponentTracer<Event> fromComponent(Component component, String suffix) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  @Override
  public ComponentTracer<Event> fromComponent(Component component, String overriddenName, String suffix) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  private static class DummyComponentTracer implements ComponentTracer<Event> {

    public static final String DUMMY_SPAN = "dummy-span";

    @Override
    public Optional<InternalSpan> startSpan(Event event) {
      return Optional.empty();
    }

    @Override
    public Optional<InternalSpan> startSpan(Event event, Assertion assertion) {
      return Optional.empty();
    }

    @Override
    public String getName() {
      return DUMMY_SPAN;
    }
  }
}
