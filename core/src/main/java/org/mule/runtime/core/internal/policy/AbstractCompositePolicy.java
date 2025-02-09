/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.policy;

import static java.util.Collections.reverse;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static reactor.core.publisher.Flux.from;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.policy.Policy;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.ReactiveProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.reactivestreams.Publisher;

/**
 * Abstract implementation that performs the chaining of a set of policies and the {@link Processor} being intercepted.
 *
 * @param <ParametersTransformer> the type of the function parameters transformer.
 *
 * @since 4.0
 */
public abstract class AbstractCompositePolicy<ParametersTransformer> {

  private final List<Policy> parameterizedPolicies;
  private final Optional<ParametersTransformer> parametersTransformer;
  private ReactiveProcessor executionProcessor;

  /**
   * Creates a new composite policy.
   *
   * @param policies              list of {@link Policy} to chain together.
   * @param parametersTransformer transformer from the operation parameters to a message and vice versa.
   */
  public AbstractCompositePolicy(List<Policy> policies,
                                 Optional<ParametersTransformer> parametersTransformer) {
    checkArgument(!policies.isEmpty(), "policies list cannot be empty");
    this.parameterizedPolicies = policies;
    this.parametersTransformer = parametersTransformer;
  }

  protected final void initProcessor() {
    this.executionProcessor = getPolicyProcessor();
  }

  protected ReactiveProcessor getPolicyProcessor() {
    List<Function<ReactiveProcessor, ReactiveProcessor>> interceptors = new ArrayList<>();
    for (Policy policy : parameterizedPolicies) {
      interceptors.add(next -> eventPub -> from(applyPolicy(policy, next, eventPub)));
    }

    ReactiveProcessor chainedPoliciesAndOperation = eventPub -> from(applyNextOperation(eventPub));
    // Take processor publisher function itself and transform it by applying interceptor transformations onto it.
    reverse(interceptors);
    for (Function<ReactiveProcessor, ReactiveProcessor> interceptor : interceptors) {
      chainedPoliciesAndOperation = interceptor.apply(chainedPoliciesAndOperation);
    }

    return chainedPoliciesAndOperation;
  }

  protected Policy getLastPolicy() {
    return parameterizedPolicies.get(parameterizedPolicies.size() - 1);
  }

  /**
   * @return the parameters transformer that converts the message to function parameters and vice versa.
   */
  protected Optional<ParametersTransformer> getParametersTransformer() {
    return parametersTransformer;
  }

  /**
   * @return the processing chain for the policy and the inner execution.
   */
  protected ReactiveProcessor getExecutionProcessor() {
    return executionProcessor;
  }

  /**
   * Template method for executing the final processor of the chain.
   *
   * @param eventPub the event to use for executing the next operation.
   * @return the event to use for processing the after phase of the policy
   * @throws MuleException if there's an error executing processing the next operation.
   */
  protected abstract Publisher<CoreEvent> applyNextOperation(Publisher<CoreEvent> eventPub);

  /**
   * Template method for executing a policy.
   *
   * @param policy        the policy to execute
   * @param nextProcessor the next processor to use as the {@link PolicyNextActionMessageProcessor}. It will invoke the next
   *                      policy in the chain.
   * @param eventPub      the event to use for processing the policy.
   * @return the result to use for the next policy in the chain.
   * @throws Exception if the execution of the policy fails.
   */
  protected abstract Publisher<CoreEvent> applyPolicy(Policy policy, ReactiveProcessor nextProcessor,
                                                      Publisher<CoreEvent> eventPub);

}
