/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.event;

import static java.util.Optional.empty;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.exception.FlowExceptionHandler;
import org.mule.runtime.core.internal.event.DefaultEventContext;
import org.mule.runtime.core.privileged.event.BaseEventContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Factory interface for creating a new {@link EventContext}
 *
 * @since 4.0
 */
public interface EventContextFactory {

  /**
   * Builds a new execution context with the given parameters.
   *
   * @param flow     the flow that processes events of this context.
   * @param location the location of the component that received the first message for this context.
   */
  static EventContext create(FlowConstruct flow, ComponentLocation location) {
    return create(flow, location, null);
  }

  /**
   * Builds a new execution context with the given parameters and an empty publisher.
   *
   * @param flow          the flow that processes events of this context.
   * @param location      the location of the component that received the first message for this context.
   * @param correlationId See {@link EventContext#getCorrelationId()}.
   */
  static EventContext create(FlowConstruct flow, ComponentLocation location, String correlationId) {
    return create(flow, location, correlationId, empty());
  }

  /**
   * Builds a new execution context with the given parameters.
   *
   * @param id               the unique id for this event context.
   * @param serverId         the id of the running mule server
   * @param location         the location of the component that received the first message for this context.
   * @param exceptionHandler the exception handler that will deal with an error context
   *
   * @deprecated Since 4.3.0, use {@link #create(String, String, ComponentLocation, String, Optional)} instead and rely on the
   *             provided {@code processor} to do the error handling.
   */
  @Deprecated
  static EventContext create(String id, String serverId, ComponentLocation location,
                             FlowExceptionHandler exceptionHandler) {
    return create(id, serverId, location, null, exceptionHandler);
  }

  /**
   * Builds a new execution context with the given parameters and an empty publisher.
   *
   * @param id               the unique id for this event context.
   * @param serverId         the id of the running mule server
   * @param location         the location of the component that received the first message for this context.
   * @param correlationId    See {@link EventContext#getCorrelationId()}.
   * @param exceptionHandler the exception handler that will deal with an error context
   *
   * @deprecated Since 4.3.0, use {@link #create(String, String, ComponentLocation, String, Optional)} instead and rely on the
   *             provided {@code processor} to do the error handling.
   */
  @Deprecated
  static EventContext create(String id, String serverId, ComponentLocation location, String correlationId,
                             FlowExceptionHandler exceptionHandler) {
    return create(id, serverId, location, correlationId, empty(), exceptionHandler);
  }

  /**
   * Builds a new execution context with the given parameters.
   *
   * @param flow               the flow that processes events of this context.
   * @param location           the location of the component that received the first message for this context.
   * @param correlationId      See {@link EventContext#getCorrelationId()}.
   * @param externalCompletion future that completes when source completes enabling termination of {@link BaseEventContext} to
   *                           depend on completion of source.
   */
  static EventContext create(FlowConstruct flow, ComponentLocation location, String correlationId,
                             Optional<CompletableFuture<Void>> externalCompletion) {
    return new DefaultEventContext(flow, location, correlationId, externalCompletion);
  }

  /**
   * Builds a new execution context with the given parameters.
   *
   * @param flow               the flow that processes events of this context.
   * @param exceptionHandler   the exception handler that will deal with an error context. This will be used instead of the one
   *                           from the given {@code flow}
   * @param location           the location of the component that received the first message for this context.
   * @param correlationId      See {@link EventContext#getCorrelationId()}.
   * @param externalCompletion future that completes when source completes enabling termination of {@link BaseEventContext} to
   *                           depend on completion of source.
   *
   * @deprecated Use {@link #create(FlowConstruct, ComponentLocation, String, Optional)} instead and rely on the provided
   *             {@code processor} to do the error handling.
   */
  @Deprecated
  static EventContext create(FlowConstruct flow, FlowExceptionHandler exceptionHandler, ComponentLocation location,
                             String correlationId, Optional<CompletableFuture<Void>> externalCompletion) {
    return new DefaultEventContext(flow, exceptionHandler, location, correlationId, externalCompletion);
  }

  /**
   * Builds a new execution context with the given parameters.
   *
   * @param id                 the unique id for this event context.
   * @param location           the location of the component that received the first message for this context.
   * @param correlationId      See {@link EventContext#getCorrelationId()}.
   * @param externalCompletion future that completes when source completes enabling termination of {@link BaseEventContext} to
   *                           depend on completion of source.
   */
  static EventContext create(String id, String serverId, ComponentLocation location, String correlationId,
                             Optional<CompletableFuture<Void>> externalCompletion) {
    return new DefaultEventContext(id, serverId, location, correlationId, externalCompletion);
  }

  /**
   * Builds a new execution context with the given parameters.
   *
   * @param id                 the unique id for this event context.
   * @param location           the location of the component that received the first message for this context.
   * @param correlationId      See {@link EventContext#getCorrelationId()}.
   * @param externalCompletion future that completes when source completes enabling termination of {@link BaseEventContext} to
   *                           depend on completion of source.
   * @param exceptionHandler   the exception handler that will deal with an error context
   *
   * @deprecated Since 4.3.0, use {@link #create(String, String, ComponentLocation, String, Optional)} instead and rely on the
   *             provided {@code processor} to do the error handling.
   */
  @Deprecated
  static EventContext create(String id, String serverId, ComponentLocation location, String correlationId,
                             Optional<CompletableFuture<Void>> externalCompletion,
                             FlowExceptionHandler exceptionHandler) {
    return new DefaultEventContext(id, serverId, location, correlationId, externalCompletion, exceptionHandler);
  }
}
