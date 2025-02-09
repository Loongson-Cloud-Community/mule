/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.tracing;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolver;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolvingContext;
import org.mule.runtime.tracer.api.component.ComponentTracer;

import java.util.Map;

/**
 * An extended {@link ResolverSet} that will trace the resolution of its composed {@link ValueResolver}s.
 * 
 * @since 4.5
 */
public class TracedResolverSet extends ResolverSet {

  private final ComponentTracer<CoreEvent> valueResolutionTracer;

  public TracedResolverSet(MuleContext muleContext,
                           ComponentTracer<CoreEvent> valueResolutionTracer) {
    super(muleContext);
    this.valueResolutionTracer = valueResolutionTracer;
  }

  @Override
  protected Object resolve(Map.Entry<String, ValueResolver<?>> entry, ValueResolvingContext valueResolvingContext)
      throws MuleException {
    valueResolutionTracer.startSpan(valueResolvingContext.getEvent());
    try {
      valueResolutionTracer.addCurrentSpanAttribute(valueResolvingContext.getEvent(), "value-name", entry.getKey());
      return super.resolve(entry, valueResolvingContext);
    } finally {
      valueResolutionTracer.endCurrentSpan(valueResolvingContext.getEvent());
    }
  }
}
