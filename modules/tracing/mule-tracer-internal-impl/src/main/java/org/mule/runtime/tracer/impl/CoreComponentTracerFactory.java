/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.tracer.impl;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.tracer.api.component.ComponentTracer;
import org.mule.runtime.tracer.api.component.ComponentTracerFactory;
import org.mule.runtime.tracer.api.span.info.InitialSpanInfo;
import org.mule.runtime.tracer.customization.api.InitialSpanInfoProvider;

import javax.inject.Inject;

public class CoreComponentTracerFactory implements ComponentTracerFactory {

  @Inject
  private InitialSpanInfoProvider initialSpanInfoProvider;

  @Inject
  private CoreEventTracer coreEventTracer;

  @Override
  public ComponentTracer fromComponent(Component component) {
    InitialSpanInfo initialSpanInfo = initialSpanInfoProvider.getInitialSpanInfo(component);
    return new CoreComponentTracer(initialSpanInfo, coreEventTracer);
  }

  @Override
  public ComponentTracer fromComponent(Component component, String suffix) {
    InitialSpanInfo initialSpanInfo = initialSpanInfoProvider.getInitialSpanInfo(component, suffix);
    return new CoreComponentTracer(initialSpanInfo, coreEventTracer);
  }

  @Override
  public ComponentTracer fromComponent(Component component, String overriddenName, String suffix) {
    InitialSpanInfo initialSpanInfo = initialSpanInfoProvider.getInitialSpanInfo(component, overriddenName, suffix);
    return new CoreComponentTracer(initialSpanInfo, coreEventTracer);
  }

}
