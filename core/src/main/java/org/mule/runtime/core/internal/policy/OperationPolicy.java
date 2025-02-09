/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.policy;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;

public interface OperationPolicy {

  /**
   * Process the policy chain of processors. The provided {@code nextOperation} function has the behaviour to be executed by the
   * next-operation of the chain.
   *
   * @param operationEvent             the event with the data to execute the operation
   * @param operationExecutionFunction the function that executes the operation.
   * @param parametersProcessor        the {@link OperationParametersProcessor} to apply
   * @param componentLocation          the location of the component on which the policy has been applied on
   * @param callback                   the {@link ExecutorCallback} on which the result of processing the {@code event} through
   *                                   the policy chain will be notified on
   */
  void process(CoreEvent operationEvent,
               OperationExecutionFunction operationExecutionFunction,
               OperationParametersProcessor parametersProcessor,
               ComponentLocation componentLocation,
               ExecutorCallback callback);

}
