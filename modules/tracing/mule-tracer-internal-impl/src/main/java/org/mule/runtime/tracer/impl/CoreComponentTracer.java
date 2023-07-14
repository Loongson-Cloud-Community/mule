/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.tracer.impl;

import org.mule.runtime.api.profiling.tracing.Span;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.tracer.api.component.ComponentTracer;
import org.mule.runtime.tracer.api.span.InternalSpan;
import org.mule.runtime.tracer.api.span.info.InitialSpanInfo;
import org.mule.runtime.tracer.api.span.validation.Assertion;

import java.util.Optional;

public class CoreComponentTracer implements ComponentTracer<CoreEvent> {

  private final InitialSpanInfo initialSpanInfo;
  private final CoreEventTracer coreEventTracer;

  public CoreComponentTracer(InitialSpanInfo initialSpanInfo, CoreEventTracer coreEventTracer) {
    this.initialSpanInfo = initialSpanInfo;
    this.coreEventTracer = coreEventTracer;
  }

  @Override
  public Optional<InternalSpan> startSpan(CoreEvent coreEvent) {
    return coreEventTracer.startComponentSpan(coreEvent, initialSpanInfo);
  }

  @Override
  public Optional<InternalSpan> startSpan(CoreEvent coreEvent, Assertion assertion) {
    return coreEventTracer.startComponentSpan(coreEvent, initialSpanInfo, assertion);
  }

  @Override
  public String getName() {
    return initialSpanInfo.getName();
  }

}
