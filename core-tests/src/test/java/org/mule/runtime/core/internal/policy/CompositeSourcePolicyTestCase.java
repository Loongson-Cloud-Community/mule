/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.policy;

import static java.lang.Runtime.getRuntime;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.runtime.core.api.rx.Exceptions.propagateWrappingFatal;
import static org.mule.runtime.core.internal.execution.SourcePolicyTestUtils.block;
import static org.mule.runtime.dsl.api.component.config.DefaultComponentLocation.fromSingleComponent;
import static reactor.core.publisher.Mono.error;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.functional.Either;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.policy.Policy;
import org.mule.runtime.core.api.policy.SourcePolicyParametersTransformer;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.ReactiveProcessor;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.execution.SourcePolicyTestUtils;
import org.mule.runtime.core.internal.message.InternalEvent;
import org.mule.runtime.core.privileged.event.BaseEventContext;
import org.mule.runtime.policy.api.PolicyPointcutParameters;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class CompositeSourcePolicyTestCase extends AbstractCompositePolicyTestCase {

  @Rule
  public ExpectedException expectedException = none();

  private CompositeSourcePolicy compositeSourcePolicy;

  private final SourcePolicyParametersTransformer sourcePolicyParametersTransformer =
      mock(SourcePolicyParametersTransformer.class);
  private final MessageSourceResponseParametersProcessor sourceParametersProcessor =
      mock(MessageSourceResponseParametersProcessor.class, RETURNS_DEEP_STUBS);

  private CoreEvent initialEvent;
  private CoreEvent modifiedEvent;
  private final Processor flowExecutionProcessor = mock(Processor.class);
  private final CoreEvent nextProcessResultEvent = mock(CoreEvent.class);
  private final SourcePolicyProcessorFactory sourcePolicyProcessorFactory =
      mock(SourcePolicyProcessorFactory.class, RETURNS_DEEP_STUBS);

  private CoreEvent flowActualResultEvent;

  public CompositeSourcePolicyTestCase(boolean policyChangeThread, boolean processChangeThread) {
    super(policyChangeThread, processChangeThread);
  }

  @Before
  public void setUp() throws Exception {
    initialEvent = createTestEvent();
    SourcePolicyContext policyContext = new SourcePolicyContext(mock(PolicyPointcutParameters.class));
    ((InternalEvent) initialEvent).setSourcePolicyContext(policyContext);

    modifiedEvent = createTestEvent();
    ((InternalEvent) initialEvent).setSourcePolicyContext(policyContext);

    when(nextProcessResultEvent.getMessage()).thenReturn(mock(Message.class));
    when(flowExecutionProcessor.apply(any())).thenAnswer(invocation -> {
      return processor().apply(invocation.getArgument(0));
    });

    when(sourcePolicyProcessorFactory.createSourcePolicy(same(firstPolicy), any())).thenAnswer(policyFactoryInvocation -> {
      return firstPolicyProcessor(policyFactoryInvocation,
                                  e -> CoreEvent.builder(e).message(modifiedEvent.getMessage()).build(),
                                  e -> CoreEvent.builder(e).message(firstPolicyResultEvent.getMessage()).build());
    });
    when(sourcePolicyProcessorFactory.createSourcePolicy(same(secondPolicy), any())).thenAnswer(policyFactoryInvocation -> {
      return secondPolicyProcessor(policyFactoryInvocation,
                                   e -> CoreEvent.builder(e).message(modifiedEvent.getMessage()).build(),
                                   e -> CoreEvent.builder(e).message(secondPolicyResultEvent.getMessage()).build());
    });
  }

  protected ReactiveProcessor processor() {
    return eventPub -> {
      Flux<CoreEvent> baseFlux = Flux.from(eventPub);
      if (processChangeThread) {
        baseFlux = baseFlux.publishOn(Schedulers.single());
      }
      return baseFlux
          .doOnNext(ev -> flowActualResultEvent = ev)
          .doOnNext(event -> ((BaseEventContext) event.getContext()).success(event));
    };
  }

  protected ReactiveProcessor errorProcessor(Throwable failure) {
    return eventPub -> {
      Flux<CoreEvent> baseFlux = Flux.from(eventPub);
      if (processChangeThread) {
        baseFlux = baseFlux.publishOn(Schedulers.single());
      }
      return baseFlux
          .doOnNext(ev -> flowActualResultEvent = ev)
          .doOnNext(event -> {
            final MessagingException me = new MessagingException(event, failure);
            ((BaseEventContext) event.getContext()).error(me);
            throw propagateWrappingFatal(me);
          });
    };
  }

  @Test
  public void singlePolicy() throws Throwable {
    compositeSourcePolicy = new CompositeSourcePolicy(asList(firstPolicy), flowExecutionProcessor,
                                                      of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory, null);

    Either<SourcePolicyFailureResult, SourcePolicySuccessResult> sourcePolicyResult =
        block(callback -> compositeSourcePolicy.process(initialEvent, sourceParametersProcessor, callback));

    assertThat(sourcePolicyResult.isRight(), is(true));
    assertThat(sourcePolicyResult.getRight().getResult().getMessage(), is(firstPolicyResultEvent.getMessage()));
    verify(flowExecutionProcessor, atLeastOnce()).apply(any());
    assertThat(flowActualResultEvent.getMessage(),
               equalTo(modifiedEvent.getMessage()));

    verify(sourcePolicyProcessorFactory, atLeastOnce()).createSourcePolicy(same(firstPolicy), any());
    assertThat(getFirstPolicyActualResultEvent().getMessage(), equalTo(initialEvent.getMessage()));
    verify(sourcePolicyParametersTransformer, never()).fromFailureResponseParametersToMessage(any());
    verify(sourcePolicyParametersTransformer).fromSuccessResponseParametersToMessage(any());
  }

  @Test
  public void compositePolicy() throws Throwable {
    compositeSourcePolicy =
        new CompositeSourcePolicy(asList(firstPolicy, secondPolicy), flowExecutionProcessor,
                                  of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory, null);

    Either<SourcePolicyFailureResult, SourcePolicySuccessResult> sourcePolicyResult =
        block(callback -> compositeSourcePolicy.process(initialEvent, sourceParametersProcessor, callback));

    assertThat(sourcePolicyResult.isRight(), is(true));
    assertThat(sourcePolicyResult.getRight().getResult().getMessage(), is(firstPolicyResultEvent.getMessage()));
    verify(flowExecutionProcessor, atLeastOnce()).apply(any());
    assertThat(flowActualResultEvent.getMessage(),
               equalTo(modifiedEvent.getMessage()));
    verify(sourcePolicyProcessorFactory, atLeastOnce()).createSourcePolicy(same(firstPolicy), any());
    verify(sourcePolicyProcessorFactory, atLeastOnce()).createSourcePolicy(same(secondPolicy), any());

    assertThat(getFirstPolicyActualResultEvent().getMessage(), equalTo(initialEvent.getMessage()));
    assertThat(getSecondPolicyActualResultEvent().getMessage(), equalTo(modifiedEvent.getMessage()));
    verify(sourcePolicyParametersTransformer, never()).fromFailureResponseParametersToMessage(any());
    verify(sourcePolicyParametersTransformer).fromSuccessResponseParametersToMessage(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyPolicyList() {
    compositeSourcePolicy = new CompositeSourcePolicy(emptyList(), flowExecutionProcessor,
                                                      of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory, null);
  }

  @Test
  public void policyExecutionFlowFailurePropagates() throws Throwable {
    RuntimeException flowException = new RuntimeException("flow failure");
    final Component policyComponent = mock(Component.class);
    when(policyComponent.getLocation()).thenReturn(fromSingleComponent("http-policy:proxy"));

    doAnswer(invocation -> {
      return Flux.from(errorProcessor(flowException).apply(invocation.getArgument(0)));
    }).when(flowExecutionProcessor).apply(any());

    compositeSourcePolicy =
        new CompositeSourcePolicy(asList(firstPolicy, secondPolicy), flowExecutionProcessor,
                                  of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory, null);

    Either<SourcePolicyFailureResult, SourcePolicySuccessResult> sourcePolicyResult =
        block(callback -> compositeSourcePolicy.process(initialEvent, sourceParametersProcessor, callback));
    assertThat(sourcePolicyResult.isLeft(), is(true));
    assertThat(sourcePolicyResult.getLeft().getMessagingException().getCause(), is(flowException));
    verify(sourcePolicyParametersTransformer, never()).fromFailureResponseParametersToMessage(any());
    verify(sourcePolicyParametersTransformer, never()).fromSuccessResponseParametersToMessage(any());
  }

  @Test
  public void policyExecutionFailureBeforeNextPropagates() throws Throwable {
    RuntimeException policyException = new RuntimeException("policy failure");
    final Component policyComponent = mock(Component.class);
    when(policyComponent.getLocation()).thenReturn(fromSingleComponent("http-policy:proxy"));

    when(sourcePolicyProcessorFactory.createSourcePolicy(same(secondPolicy), any())).thenAnswer(policyFactoryInvocation -> {
      Processor secondPolicySourcePolicyProcessor = mock(Processor.class);
      when(secondPolicySourcePolicyProcessor.apply(any(Publisher.class)))
          .thenAnswer(inv -> {
            Flux<CoreEvent> baseFlux = Flux.from(inv.getArgument(0));
            if (policyChangeThread) {
              baseFlux = baseFlux.publishOn(Schedulers.single());
            }
            return baseFlux.flatMap(event -> error(new MessagingException(event, policyException, policyComponent)));
          });
      return secondPolicySourcePolicyProcessor;
    });

    compositeSourcePolicy =
        new CompositeSourcePolicy(asList(firstPolicy, secondPolicy), flowExecutionProcessor,
                                  of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory, null);

    Either<SourcePolicyFailureResult, SourcePolicySuccessResult> sourcePolicyResult =
        block(callback -> compositeSourcePolicy.process(initialEvent, sourceParametersProcessor, callback));
    assertThat(sourcePolicyResult.isLeft(), is(true));
    assertThat(sourcePolicyResult.getLeft().getMessagingException().getCause(), is(policyException));
    verify(sourcePolicyParametersTransformer, never()).fromFailureResponseParametersToMessage(any());
    verify(sourcePolicyParametersTransformer, never()).fromSuccessResponseParametersToMessage(any());
  }

  @Test
  public void policyExecutionFailureAfterNextPropagates() throws Throwable {
    RuntimeException policyException = new RuntimeException("policy failure");
    final Component policyComponent = mock(Component.class);
    when(policyComponent.getLocation()).thenReturn(fromSingleComponent("http-policy:proxy"));

    when(sourcePolicyProcessorFactory.createSourcePolicy(same(secondPolicy), any())).thenAnswer(policyFactoryInvocation -> {
      final Processor policyProcessor = secondPolicyProcessor(policyFactoryInvocation,
                                                              e -> CoreEvent.builder(e).message(modifiedEvent.getMessage())
                                                                  .build(),
                                                              e -> CoreEvent.builder(e)
                                                                  .message(secondPolicyResultEvent.getMessage()).build());

      Processor secondPolicySourcePolicyProcessor = mock(Processor.class);
      when(secondPolicySourcePolicyProcessor.apply(any(Publisher.class)))
          .thenAnswer(inv -> {
            Flux<CoreEvent> baseFlux = Flux.from(inv.getArgument(0));
            if (policyChangeThread) {
              baseFlux = baseFlux.publishOn(Schedulers.single());
            }
            return baseFlux
                .transform(policyProcessor)
                .flatMap(event -> error(new MessagingException(event, policyException, policyComponent)));
          });
      return secondPolicySourcePolicyProcessor;
    });

    compositeSourcePolicy =
        new CompositeSourcePolicy(asList(firstPolicy, secondPolicy), flowExecutionProcessor,
                                  of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory, null);

    Either<SourcePolicyFailureResult, SourcePolicySuccessResult> sourcePolicyResult =
        block(callback -> compositeSourcePolicy.process(initialEvent, sourceParametersProcessor, callback));
    assertThat(sourcePolicyResult.isLeft(), is(true));
    assertThat(sourcePolicyResult.getLeft().getMessagingException().getCause(), is(policyException));
    verify(sourcePolicyParametersTransformer, never()).fromFailureResponseParametersToMessage(any());
    verify(sourcePolicyParametersTransformer).fromSuccessResponseParametersToMessage(any());
  }

  @Test
  public void reactorPipelinesReused() throws Throwable {
    InvocationsRecordingCompositeSourcePolicy.reset();
    final InvocationsRecordingCompositeSourcePolicy sourcePolicy =
        new InvocationsRecordingCompositeSourcePolicy(asList(firstPolicy), flowExecutionProcessor,
                                                      of(sourcePolicyParametersTransformer), sourcePolicyProcessorFactory);

    assertThat(sourcePolicy.getNextOperationCount(), is(getRuntime().availableProcessors()));
    assertThat(sourcePolicy.getPolicyCount(), is(getRuntime().availableProcessors()));

    for (int i = 0; i < getRuntime().availableProcessors() * 2; ++i) {
      SourcePolicyTestUtils.<Either<SourcePolicyFailureResult, SourcePolicySuccessResult>>block(
                                                                                                callback -> sourcePolicy
                                                                                                    .process(initialEvent,
                                                                                                             sourceParametersProcessor,
                                                                                                             callback));
    }

    assertThat(sourcePolicy.getNextOperationCount(), is(getRuntime().availableProcessors()));
    assertThat(sourcePolicy.getPolicyCount(), is(getRuntime().availableProcessors()));
  }

  public static final class InvocationsRecordingCompositeSourcePolicy extends CompositeSourcePolicy {

    private static final AtomicInteger nextOperation = new AtomicInteger();
    private static final AtomicInteger policy = new AtomicInteger();

    public InvocationsRecordingCompositeSourcePolicy(List<Policy> parameterizedPolicies, ReactiveProcessor flowExecutionProcessor,
                                                     Optional<SourcePolicyParametersTransformer> sourcePolicyParametersTransformer,
                                                     SourcePolicyProcessorFactory sourcePolicyProcessorFactory) {
      super(parameterizedPolicies, flowExecutionProcessor, sourcePolicyParametersTransformer, sourcePolicyProcessorFactory, null);
    }

    public static void reset() {
      nextOperation.set(0);
      policy.set(0);
    }

    @Override
    protected Publisher<CoreEvent> applyNextOperation(Publisher<CoreEvent> eventPub) {
      nextOperation.incrementAndGet();
      return super.applyNextOperation(eventPub);
    }

    @Override
    protected Publisher<CoreEvent> applyPolicy(Policy policy, ReactiveProcessor nextProcessor, Publisher<CoreEvent> eventPub) {
      InvocationsRecordingCompositeSourcePolicy.policy.incrementAndGet();
      return super.applyPolicy(policy, nextProcessor, eventPub);
    }

    public int getNextOperationCount() {
      return nextOperation.get();
    }

    public int getPolicyCount() {
      return policy.get();
    }
  }
}
