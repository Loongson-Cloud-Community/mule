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
import org.mule.runtime.tracer.api.span.InternalSpan;
import org.mule.runtime.tracer.api.span.validation.Assertion;

import java.util.Optional;

/**
 * Component Tracer for starting a {@link Span}.
 *
 * @param <T> an {@link Event}.
 */
public interface ComponentTracer<T extends Event> {

  /**
   * Starts a {@link Span} associated to the {@link Component}.
   *
   * @param event the {@link Event} that has hit the {@link Component}.
   * @return the {@link Span} generated for the context of the {@link Event} when it hits the {@link Component} if it could be
   *         created.
   */
  Optional<InternalSpan> startSpan(T event);

  /**
   * Starts a {@link Span} associated to the {@link Component}.
   *
   * @param event     the {@link Event} that has hit the {@link Component}.
   * @param assertion indicates a condition that has to be verified for starting the span.
   * @return the {@link Span} generated for the context of the {@link Event} when it hits the {@link Component} if it could be
   *         created.
   */
  Optional<InternalSpan> startSpan(T event, Assertion assertion);

  /**
   * Obtains the name of the {@link Span} and returns it.
   *
   * @return the name of the {@link Span}.
   */
  String getName();

}
