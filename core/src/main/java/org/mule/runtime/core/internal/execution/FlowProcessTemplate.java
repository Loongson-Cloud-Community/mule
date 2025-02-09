/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.execution;

import org.mule.runtime.api.component.execution.CompletableCallback;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.functional.Either;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.source.MessageSource;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.policy.MessageSourceResponseParametersProcessor;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

/**
 * Template methods for {@link MessageSource} specific behavior during flow execution.
 */
public interface FlowProcessTemplate extends MessageSourceResponseParametersProcessor {

  /**
   * @return a {@link SourceResultAdapter} created from the original message
   */
  SourceResultAdapter getSourceMessage();

  /**
   * @return a {@link List} of {@link NotificationFunction} to evaluate and fire
   * @since 4.1
   */
  List<NotificationFunction> getNotificationFunctions();

  /**
   * Routes the {@link CoreEvent} through the processors chain
   *
   * @param event {@link CoreEvent} created from the raw message of this context
   * @return the response {@link CoreEvent}
   * @throws MuleException
   */
  CoreEvent routeEvent(CoreEvent event) throws MuleException;

  /**
   * Routes the {@link CoreEvent} through the processors chain using async API.
   *
   * @param event {@link CoreEvent} created from the raw message of this context
   * @return the {@link Publisher} that will ne signalled on processing completion
   */
  Publisher<CoreEvent> routeEventAsync(CoreEvent event);

  /**
   * Routes the {@link CoreEvent} through the processors chain using async API.
   *
   * @param eventPub a {@link Publisher} of the {@link CoreEvent} created from the raw message of this context
   * @return the {@link Publisher} that will be signaled on processing completion
   */
  Publisher<CoreEvent> routeEventAsync(Publisher<CoreEvent> eventPub);

  /**
   * Template method to send a response after processing the message.
   * <p>
   * This method is executed within the flow so if it fails it will trigger the exception strategy.
   *
   * @param response   the result of the flow execution
   * @param parameters the resolved set of parameters required to send the response.
   * @param callback   the callback used to signal completion
   */
  void sendResponseToClient(CoreEvent response, Map<String, Object> parameters, CompletableCallback<Void> callback);


  /**
   * Template method to send a failure response after processing the message.
   *
   * @param exception  exception thrown during the flow execution.
   * @param parameters the resolved set of parameters required to send the failure response.
   * @param callback   the callback used to signal completion
   */
  void sendFailureResponseToClient(MessagingException exception,
                                   Map<String, Object> parameters,
                                   CompletableCallback<Void> callback);

  /**
   * Template method to be executed after the flow completes it's execution including any policy that may be applied.
   * <p/>
   * This method will always be executed and the {@code either} parameter will indicate the result of the execution.
   *
   * @param either that communicates the result of the flow execution.
   *               <ul>
   *               <li>{@link CoreEvent} if the execution finished correctly</li>
   *               <li>{@link MessagingException} if an error occurred during the execution</li>
   *               </ul>
   */
  void afterPhaseExecution(Either<MessagingException, CoreEvent> either);
}
