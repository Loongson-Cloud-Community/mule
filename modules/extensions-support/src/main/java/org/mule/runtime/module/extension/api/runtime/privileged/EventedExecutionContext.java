/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.api.runtime.privileged;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;

/**
 * A specialization of {@link ExecutionContext} which contains an {@link CoreEvent}
 *
 * @param <M> the generic type of of the model which represents the component being executed
 * @since 4.0
 */
@NoImplement
public interface EventedExecutionContext<M extends ComponentModel> extends ExecutionContext<M> {

  /**
   * Returns the {@link CoreEvent} on which an operation is to be executed
   */
  CoreEvent getEvent();

  /**
   * Changes the {@link CoreEvent} on which an operation is to be executed. Not null.
   *
   * @param updated the event to use
   */
  void changeEvent(CoreEvent updated);

}
