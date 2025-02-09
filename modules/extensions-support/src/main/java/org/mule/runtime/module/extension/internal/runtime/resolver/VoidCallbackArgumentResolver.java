/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static org.mule.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties.COMPLETION_CALLBACK_CONTEXT_PARAM;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.process.VoidCompletionCallback;
import org.mule.runtime.module.extension.api.runtime.privileged.EventedResult;
import org.mule.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

/**
 * {@link ArgumentResolver} which returns the
 * {@link org.mule.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties#COMPLETION_CALLBACK_CONTEXT_PARAM}
 * context variable.
 * <p>
 * Notice that this resolver only works if the {@link ExecutionContext} is a {@link ExecutionContextAdapter}
 *
 * @since 4.1
 */
public final class VoidCallbackArgumentResolver implements ArgumentResolver<VoidCompletionCallback> {

  @Override
  public VoidCompletionCallback resolve(ExecutionContext executionContext) {
    ExecutionContextAdapter adapter = (ExecutionContextAdapter) executionContext;
    CompletionCallback completionCallback = (CompletionCallback) adapter.getVariable(COMPLETION_CALLBACK_CONTEXT_PARAM);
    final CoreEvent event = adapter.getEvent();

    return new VoidCompletionCallback() {

      @Override
      public void success() {
        completionCallback.success(EventedResult.from(event));
      }

      @Override
      public void error(Throwable e) {
        completionCallback.error(e);
      }
    };
  }
}
