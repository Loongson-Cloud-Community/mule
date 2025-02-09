/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.policy;

import static org.mule.runtime.core.internal.policy.PolicyNextActionMessageProcessor.POLICY_IS_PROPAGATE_MESSAGE_TRANSFORMATIONS;
import static org.mule.runtime.core.internal.policy.PolicyNextActionMessageProcessor.POLICY_NEXT_OPERATION;
import static reactor.core.publisher.Flux.from;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.policy.Policy;
import org.mule.runtime.core.api.policy.PolicyChain;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.ReactiveProcessor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.reactivestreams.Publisher;

/**
 * This class is responsible for the processing of a policy applied to a {@link org.mule.runtime.core.api.source.MessageSource}.
 * <p>
 * In order for this class to be able to execute a policy it requires an {@link PolicyChain} with the content of the policy. Such
 * policy may have an {@link PolicyNextActionMessageProcessor} which will be the one used to execute the provided
 * {@link Processor} which may be another policy or the actual logic behind the
 * {@link org.mule.runtime.core.api.source.MessageSource} which typically is a flow execution.
 * <p>
 * This class enforces the scoping of variables between the actual behaviour and the policy that may be applied to it. To enforce
 * such scoping of variables it uses internal parameters so the last {@link CoreEvent} modified by the policy behaviour can be
 * stored and retrieve for later usages. It also uses {@code PolicyEventConverter} as a helper class to convert an
 * {@link CoreEvent} from the policy to the next operation {@link CoreEvent} or from the next operation result to the
 * {@link CoreEvent} that must continue the execution of the policy.
 * <p/>
 * If a non-empty {@code sourcePolicyParametersTransformer} is passed to this class, then it will be used to convert the result of
 * the policy chain execution to the set of parameters that the success response function or the failure response function will be
 * used to execute.
 */
public class SourcePolicyProcessor implements ReactiveProcessor {

  private final Policy policy;
  private final Reference<ReactiveProcessor> nextProcessorRef;
  private final PolicyEventMapper policyEventMapper;

  /**
   * Creates a new {@code DefaultSourcePolicy}.
   *
   * @param policy        the policy to execute before and after the source.
   * @param nextProcessor the next-operation processor implementation, it may be another policy or the flow execution.
   */
  public SourcePolicyProcessor(Policy policy, ReactiveProcessor nextProcessor) {
    this.policy = policy;
    this.nextProcessorRef = new WeakReference<>(nextProcessor);
    this.policyEventMapper = new PolicyEventMapper(policy.getPolicyId());
  }

  /**
   * Process the source policy chain of processors. The provided {@code nextOperation} function has the behaviour to be executed
   * by the next-operation of the chain which may be the next policy in the chain or the flow execution.
   *
   * @return the result of processing the {@code event} through the policy chain.
   */
  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return from(publisher)
        .map(policyEventMapper::onSourcePolicyBegin)
        .transform(policy.getPolicyChain())
        .subscriberContext(ctx -> ctx
            .put(POLICY_NEXT_OPERATION, nextProcessorRef)
            .put(POLICY_IS_PROPAGATE_MESSAGE_TRANSFORMATIONS, policy.getPolicyChain().isPropagateMessageTransformations()));
  }

}
