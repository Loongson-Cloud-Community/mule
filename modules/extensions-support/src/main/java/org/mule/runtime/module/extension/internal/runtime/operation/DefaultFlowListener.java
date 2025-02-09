/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.operation;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.slf4j.LoggerFactory.getLogger;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.privileged.event.BaseEventContext;
import org.mule.runtime.extension.api.runtime.operation.FlowListener;

import java.util.function.Consumer;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;

/**
 * Default implementation of {@link FlowListener}.
 * <p>
 * It uses an {@link CoreEvent}'s response {@link Publisher} to subscribe to the event termination and execute the necessary
 * logic.
 *
 * @since 4.0
 */
public class DefaultFlowListener implements FlowListener {

  private static final Logger LOGGER = getLogger(DefaultFlowListener.class);

  private final ExtensionModel extensionModel;
  private final OperationModel operationModel;

  private Consumer<Message> successConsumer;
  private Consumer<Exception> errorConsumer;
  private Runnable onComplete;

  /**
   * Creates a new instance
   *
   * @param event the event on which the operation is being executed.
   */
  public DefaultFlowListener(ExtensionModel extensionModel, OperationModel operationModel, CoreEvent event) {
    this.extensionModel = extensionModel;
    this.operationModel = operationModel;
    BaseEventContext context = (BaseEventContext) event.getContext();
    context.onResponse(this::onResponse);
    context.getRootContext().onTerminated((e, t) -> onTerminated());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onSuccess(Consumer<Message> handler) {
    assertNotNull(handler);
    successConsumer = handler;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onError(Consumer<Exception> handler) {
    assertNotNull(handler);
    this.errorConsumer = handler;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onComplete(Runnable handler) {
    assertNotNull(handler);
    onComplete = handler;
  }

  private void onResponse(CoreEvent event, Throwable error) {
    if (event != null && successConsumer != null) {
      try {
        successConsumer.accept(event.getMessage());
      } catch (Exception e) {
        LOGGER.warn("Operation " + operationModel.getName() + " from extension " + extensionModel.getName()
            + " threw exception while executing the onSuccess FlowListener", e);
      }
    } else if (error != null && errorConsumer != null) {
      Exception exception = error instanceof Exception ? (Exception) error : new MessagingException(event, error);
      try {
        errorConsumer.accept(exception);
      } catch (Exception e) {
        LOGGER.warn("Operation " + operationModel.getName() + " from extension " + extensionModel.getName()
            + " threw exception while executing the onError FlowListener", e);
      }
    }
  }

  private void onTerminated() {
    if (onComplete != null) {
      try {
        onComplete.run();
      } catch (Exception e) {
        LOGGER.warn("Operation " + operationModel.getName() + " from extension " + extensionModel.getName()
            + " threw exception while executing the onComplete FlowListener", e);
      }
    }
  }

  private void assertNotNull(Object handler) {
    checkArgument(handler != null, "Cannot set null handler");
  }
}
