/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.processor;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.core.api.construct.BackPressureReason;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.event.BaseEventContext;

import java.util.function.Consumer;

/**
 * Used to dispatch {@link CoreEvent}'s asynchronously for processing. The result of asynchronous processing can be obtained by
 * subscribing to the {@link CoreEvent}'s {@link BaseEventContext}.
 * <p/>
 * All Sinks must support concurrent calls from multiple publishers and it is then up to each implementation to determine how to
 * handle this, i.e.
 * <ol>
 * <li>By continuing in the caller thread.</li>
 * <li>Serializing all events to a single thread.</li>
 * <li>Using a ring-buffer to de-multiplex requests and then handle them with 1..n subscribers.</li>
 * </ol>
 *
 * @since 4.0
 */
@NoImplement
public interface Sink extends Consumer<CoreEvent> {

  /**
   * Submit the given {@link CoreEvent} for processing without a timeout. If the {@link CoreEvent} cannot be processed immediately
   * due to back-pressure then this method will block until in can be processed.
   *
   * @param event the {@link CoreEvent} to dispatch for processing
   */
  @Override
  void accept(CoreEvent event);

  /**
   * Submit the given {@link CoreEvent} for processing. If the {@link CoreEvent} cannot be processed immediately due to
   * back-pressure then this method will return {@code false}.
   *
   * @param event the {@link CoreEvent} to dispatch for processing
   * @return {@code null} is the {@link CoreEvent} was submitted for processing successfully, Otherwise, the reason why it was not
   *         submitted.
   */
  BackPressureReason emit(CoreEvent event);

}
