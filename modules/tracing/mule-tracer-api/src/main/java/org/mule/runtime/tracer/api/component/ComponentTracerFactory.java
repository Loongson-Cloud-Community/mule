/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.tracer.api.component;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.profiling.tracing.Span;

/**
 * Component Tracer Factory for creating {@link ComponentTracer}s.
 *
 * @since 4.5.0
 */
public interface ComponentTracerFactory {

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component the {@link Component} processor.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<Event> fromComponent(Component component);

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component the {@link Component} processor.
   * @param suffix    the suffix of the {@link Component} name.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<Event> fromComponent(Component component, String suffix);

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component      the {@link Component} processor.
   * @param overriddenName the overridden name of the {@link Component} name.
   * @param suffix         the suffix of the {@link Component} name.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<Event> fromComponent(Component component, String overriddenName, String suffix);

}
