/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.core.internal.profiling.tracing;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.tracer.api.span.info.ComponentSpanInfo;
import org.mule.runtime.tracer.api.span.info.InitialSpanInfo;
import org.mule.runtime.tracer.customization.api.InitialSpanInfoProvider;

public class ProcessorComponentSpanInfo extends AbstractComponent implements ComponentSpanInfo {

  private final InitialSpanInfo initialSpanInfo;

  public ProcessorComponentSpanInfo(InitialSpanInfoProvider initialSpanInfoProvider, Component component) {
    this.initialSpanInfo = initialSpanInfoProvider.getInitialSpanInfo(component);
  }

  public ProcessorComponentSpanInfo(InitialSpanInfoProvider initialSpanInfoProvider, Component component, String suffix) {
    this.initialSpanInfo = initialSpanInfoProvider.getInitialSpanInfo(component, suffix);
  }

  public ProcessorComponentSpanInfo(InitialSpanInfoProvider initialSpanInfoProvider, Component component, String overriddenName,
                                    String suffix) {
    this.initialSpanInfo = initialSpanInfoProvider.getInitialSpanInfo(component, overriddenName, suffix);
  }

  public InitialSpanInfo getInitialSpanInfo() {
    return this.initialSpanInfo;
  }

  public String getName() {
    return this.initialSpanInfo.getName();
  }
}
