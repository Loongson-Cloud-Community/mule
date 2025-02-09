/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.policy;

import org.mule.runtime.api.component.execution.CompletableCallback;
import org.mule.runtime.api.functional.Either;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;

/**
 * Interceptor of a {@link Processor} that executes logic before and after it. It allows to modify the content of the response (if
 * any) to be sent by a {@link org.mule.runtime.core.api.source.MessageSource}
 *
 * @since 4.0
 */
public interface SourcePolicy {

  /**
   * Process the source policy chain of processors. The provided {@code nextOperation} function has the behaviour to be executed
   * by the next-operation of the chain which may be the next policy in the chain or the flow execution.
   *
   * @param sourceEvent                              the event with the data created from the source message that must be used to
   *                                                 execute the source policy. execute the successful or failure response
   *                                                 function of the source.
   * @param messageSourceResponseParametersProcessor processor to generate the response and error response parameters of the
   *                                                 source.
   * @param callback                                 the callback used to signal the result of processing the {@code event}
   *                                                 through the policy chain.
   */
  void process(CoreEvent sourceEvent,
               MessageSourceResponseParametersProcessor messageSourceResponseParametersProcessor,
               CompletableCallback<Either<SourcePolicyFailureResult, SourcePolicySuccessResult>> callback);

}
