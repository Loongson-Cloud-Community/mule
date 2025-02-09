/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.result;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;
import org.mule.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

/**
 * An implementation of {@link ReturnDelegate} intended for operations which return {@link Void} and that were executed with a
 * {@link ExecutionContextAdapter}
 * <p/>
 * It returns the {@link CoreEvent} that {@link ExecutionContextAdapter} provides. Notices that this class will fail if used with
 * any other type of {@link ExecutionContext}
 * <p/>
 * This class is intended to be used as a singleton, use the {@link #INSTANCE} attribute to access the instance
 *
 * @since 3.7.0
 */
public final class VoidReturnDelegate implements ReturnDelegate {

  public static final ReturnDelegate INSTANCE = new VoidReturnDelegate();

  private VoidReturnDelegate() {}

  /**
   * {@inheritDoc}
   *
   * @return {@link ExecutionContextAdapter#getEvent()}
   */
  @Override
  public CoreEvent asReturnValue(Object value, ExecutionContextAdapter operationContext) {
    CoreEvent event = operationContext.getEvent();
    if (event.getSecurityContext() != operationContext.getSecurityContext()) {
      return CoreEvent.builder(event).securityContext(operationContext.getSecurityContext()).build();
    }

    return event;
  }
}
