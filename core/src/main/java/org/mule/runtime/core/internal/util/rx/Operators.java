/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.rx;

import static org.mule.runtime.api.el.BindingContextUtils.getTargetBindingContext;
import static org.mule.runtime.core.api.event.CoreEvent.builder;

import org.mule.runtime.api.el.CompiledExpression;
import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.api.el.ExpressionLanguageSession;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.event.BaseEventContext;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

/**
 * Reusable operators to be use with project reactor.
 */
public final class Operators {

  private Operators() {}

  /**
   * Custom function to be used with {@link reactor.core.publisher.Flux#handle(BiConsumer)} when a map function may return
   * {@code null} and this should be interpreted as empty rather than causing an error. If null is return by the function then the
   * {@link BaseEventContext} is also completed.
   *
   * @param mapper map function
   * @return custom operator {@link BiConsumer} to be used with {@link reactor.core.publisher.Flux#handle(BiConsumer)}.
   */
  public static BiConsumer<CoreEvent, SynchronousSink<CoreEvent>> nullSafeMap(Function<CoreEvent, CoreEvent> mapper) {
    return (event, sink) -> {
      try {
        if (event != null) {
          CoreEvent result = mapper.apply(event);
          if (result != null) {
            sink.next(result);
          } else {
            ((BaseEventContext) event.getContext()).success();
          }
        }
      } catch (Exception e) {
        sink.error(e);
      }
    };
  }

  /**
   * Returns a function that transforms a {@link CoreEvent} into another one in which the result of evaluating the
   * {@code targetValueExpression} over the {@code originalEvent} is added as a variable of key {@code target}.
   *
   * @param originalEvent         the event on which the expression is evaluated
   * @param target                the name of the variable in which the result is put
   * @param targetValueExpression the expression to evaluate
   * @param expressionManager     the {@link ExpressionManager} used for the evaluation
   * @return a {@link Function}
   * @deprecated since 4.3.0. Use {@link #outputToTarget(CoreEvent, CoreEvent, String, CompiledExpression, ExpressionLanguage)}
   *             instead
   */
  @Deprecated
  public static Function<CoreEvent, CoreEvent> outputToTarget(CoreEvent originalEvent, String target,
                                                              String targetValueExpression,
                                                              ExpressionLanguage expressionManager) {
    return result -> {
      if (target != null) {
        TypedValue targetValue = expressionManager.evaluate(targetValueExpression, getTargetBindingContext(result.getMessage()));
        return builder(originalEvent).addVariable(target, targetValue).build();
      } else {
        return result;
      }
    };
  }

  /**
   * Returns a new {@code CoreEvent}in which the result of evaluating the {@code targetValueExpression} over the
   * {@code originalEvent} was added as a variable of key {@code target}.
   *
   * @param originalEvent         the event on which the expression is evaluated
   * @param target                the name of the variable in which the result is put
   * @param targetValueExpression the expression to evaluate
   * @return a new {@link CoreEvent}
   */
  public static CoreEvent outputToTarget(CoreEvent originalEvent,
                                         CoreEvent result,
                                         String target,
                                         CompiledExpression targetValueExpression,
                                         ExpressionLanguage expressionLanguage) {
    if (target != null) {
      try (ExpressionLanguageSession session = expressionLanguage.openSession(getTargetBindingContext(result.getMessage()))) {
        TypedValue targetValue = session.evaluate(targetValueExpression);
        return builder(originalEvent)
            .addVariable(target, targetValue)
            .build();
      }
    } else {
      return result;
    }
  }

  /**
   * Return a singleton {@link Subscriber} that does not check for double onSubscribe and purely request Long.MAX. Unlike using
   * {@link Flux#subscribe()} directly this will not throw an exception if an error occurs.
   *
   * @return a new {@link Subscriber} whose sole purpose is to request Long.MAX
   */
  @SuppressWarnings("unchecked")
  public static <T> Subscriber<T> requestUnbounded() {
    return RequestMaxSubscriber.INSTANCE;
  }

  final static class RequestMaxSubscriber<T> implements Subscriber<T> {

    static final RequestMaxSubscriber INSTANCE = new RequestMaxSubscriber();

    @Override
    public void onSubscribe(Subscription s) {
      s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Object o) {

    }

    @Override
    public void onError(Throwable t) {}

    @Override
    public void onComplete() {

    }
  }
}


