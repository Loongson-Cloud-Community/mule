/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.exception;

import static org.mule.runtime.core.api.rx.Exceptions.propagateWrappingFatal;

import static java.util.Collections.emptyMap;

import static reactor.core.publisher.Flux.error;
import static reactor.core.publisher.Mono.just;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.exception.MessagingException;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Publisher;

/**
 * Take some action when an exception has occurred while executing a Flow for an event.
 */
@NoImplement
public interface FlowExceptionHandler extends Function<Exception, Publisher<CoreEvent>> {

  /**
   * Take some action when a messaging exception has occurred (i.e., there was a message in play when the exception occurred).
   *
   * @param exception which occurred
   * @param event     which was being processed when the exception occurred
   * @return new event to route on to the rest of the flow, generally with ExceptionPayload set on the message
   * @deprecated Use {@link FlowExceptionHandler#router(Function, Consumer, Consumer)}
   */
  @Deprecated
  CoreEvent handleException(Exception exception, CoreEvent event);

  /**
   * @param exception the exception to handle
   * @return the publisher with the handling result
   * @deprecated Use {@link FlowExceptionHandler#router(Function, Consumer, Consumer)}
   */
  @Override
  @Deprecated
  default Publisher<CoreEvent> apply(Exception exception) {
    try {
      if (exception instanceof MessagingException) {
        MessagingException me = (MessagingException) exception;
        me.setProcessedEvent(handleException(exception, me.getEvent()));
        if (me.handled()) {
          return just(me.getEvent());
        } else {
          return error(exception);
        }
      } else {
        return error(exception);
      }
    } catch (Throwable throwable) {
      return error(propagateWrappingFatal(throwable));
    }
  }

  /**
   * Provides a router for an error towards the destination error handler, calling the corresponding callback in case of failure
   * or success.
   *
   * @param publisherPostProcessor allows to modify the publisher that will handle the error.
   * @param continueCallback       the callback called in case the error is successfully handled (handling logic executed without
   *                               errors).
   * @param propagateCallback      the callback is called in case the error-handling fails (handling logic threw an error)
   * @return the router for an error.
   *
   * @since 4.3
   */
  default Consumer<Exception> router(Function<Publisher<CoreEvent>, Publisher<CoreEvent>> publisherPostProcessor,
                                     Consumer<CoreEvent> continueCallback,
                                     Consumer<Throwable> propagateCallback) {
    return error -> propagateCallback.accept(error);
  }

  /**
   * Get the mapping for the routers associated with the chains for each flow in the global error handler.
   *
   * @return the mapping from chains to routers.
   */
  default Map<Component, Consumer<Exception>> getRouters() {
    return emptyMap();
  }
}

