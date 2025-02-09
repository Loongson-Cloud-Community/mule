/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.construct;

import org.mule.runtime.api.component.Component;

import java.util.concurrent.RejectedExecutionException;

import static org.mule.runtime.core.api.construct.BackPressureReason.EVENTS_ACCUMULATED;

/**
 * The flow is already processing the number of events required by its maxConcurrency.
 *
 * @since 4.3, 4.2.2
 */
public class FlowBackPressureEventsAccumulatedException extends FlowBackPressureException {

  private static final long serialVersionUID = 7149961265248945243L;

  /**
   * Create a new {@link FlowBackPressureEventsAccumulatedException} with no cause. This is typically use when a stream based
   * processing exerts back-pressure without throwing an exception.
   */
  public FlowBackPressureEventsAccumulatedException(Component flow) {
    super(flow, EVENTS_ACCUMULATED);
  }

  /**
   * Create a new {@link FlowBackPressureEventsAccumulatedException} with a cause. This is typically use when a non-stream based
   * processing strategy is in use and back-pressure is identified by a {@link RejectedExecutionException}.
   */
  public FlowBackPressureEventsAccumulatedException(Component flow, Throwable cause) {
    super(flow, EVENTS_ACCUMULATED, cause);
  }

}
