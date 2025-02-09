/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.tracer.exporter.impl;

import org.mule.runtime.api.profiling.tracing.SpanIdentifier;

/**
 * a {@link SpanIdentifier} based on the open telemetry span.
 *
 * @since 4.5.0
 */
public class OpentelemetrySpanIdentifier implements SpanIdentifier {

  private final String spanId;
  private final String traceId;

  public OpentelemetrySpanIdentifier(String spanId, String traceId) {
    this.spanId = spanId;
    this.traceId = traceId;
  }

  @Override
  public String getId() {
    return spanId;
  }

  @Override
  public String getTraceId() {
    return traceId;
  }
}
