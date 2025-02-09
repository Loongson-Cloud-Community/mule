/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.exception;

import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CLIENT_SECURITY;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.COMPOSITE_ROUTING;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CONNECTIVITY;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.DUPLICATE_MESSAGE;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.EXPRESSION;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.NOT_PERMITTED;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.REDELIVERY_EXHAUSTED;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.RETRY_EXHAUSTED;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.ROUTING;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.SECURITY;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.SERVER_SECURITY;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.STREAM_MAXIMUM_SIZE_EXCEEDED;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.TRANSFORMATION;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.UNKNOWN;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.VALIDATION;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FATAL;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FLOW_BACK_PRESSURE;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.OVERLOAD;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.MuleFatalException;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.api.security.ClientSecurityException;
import org.mule.runtime.api.security.NotPermittedException;
import org.mule.runtime.api.security.SecurityException;
import org.mule.runtime.api.security.ServerSecurityException;
import org.mule.runtime.api.streaming.exception.StreamingBufferSizeExceededException;
import org.mule.runtime.core.api.exception.ExceptionMapper;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.mule.runtime.core.api.retry.policy.RetryPolicyExhaustedException;
import org.mule.runtime.core.api.transformer.MessageTransformerException;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.internal.construct.FlowBackPressureException;
import org.mule.runtime.core.internal.event.EventContextDeepNestingException;
import org.mule.runtime.core.internal.routing.split.DuplicateMessageException;
import org.mule.runtime.core.privileged.exception.ErrorTypeLocator;
import org.mule.runtime.core.privileged.exception.MessageRedeliveredException;
import org.mule.runtime.core.privileged.routing.CompositeRoutingException;
import org.mule.runtime.core.privileged.routing.RoutingException;

import java.util.concurrent.RejectedExecutionException;

/**
 * Factory for {@link ErrorTypeLocator}.
 *
 * @since 4.0
 */
public class ErrorTypeLocatorFactory {

  /**
   * Creates the default {@link ErrorTypeLocator} to use in mule.
   *
   * @param errorTypeRepository error type repository. Commonly created using {@link ErrorTypeRepositoryFactory}.
   * @return a new {@link ErrorTypeLocator}.
   */
  public static ErrorTypeLocator createDefaultErrorTypeLocator(ErrorTypeRepository errorTypeRepository) {
    ErrorType unknown = errorTypeRepository.getErrorType(UNKNOWN).get();
    return ErrorTypeLocator.builder(errorTypeRepository)
        .defaultExceptionMapper(ExceptionMapper.builder()
            .addExceptionMapping(MessageTransformerException.class, errorTypeRepository.lookupErrorType(TRANSFORMATION).get())
            .addExceptionMapping(TransformerException.class, errorTypeRepository.lookupErrorType(TRANSFORMATION).get())
            .addExceptionMapping(ExpressionRuntimeException.class, errorTypeRepository.lookupErrorType(EXPRESSION).get())
            .addExceptionMapping(RoutingException.class, errorTypeRepository.lookupErrorType(ROUTING).get())
            .addExceptionMapping(CompositeRoutingException.class, errorTypeRepository.getErrorType(COMPOSITE_ROUTING).get())
            .addExceptionMapping(ConnectionException.class, errorTypeRepository.lookupErrorType(CONNECTIVITY).get())
            .addExceptionMapping(ValidationException.class, errorTypeRepository.lookupErrorType(VALIDATION).get())
            .addExceptionMapping(DuplicateMessageException.class, errorTypeRepository.lookupErrorType(DUPLICATE_MESSAGE).get())
            .addExceptionMapping(RetryPolicyExhaustedException.class, errorTypeRepository.lookupErrorType(RETRY_EXHAUSTED).get())
            .addExceptionMapping(SecurityException.class, errorTypeRepository.lookupErrorType(SECURITY).get())
            .addExceptionMapping(ClientSecurityException.class, errorTypeRepository.lookupErrorType(CLIENT_SECURITY).get())
            .addExceptionMapping(ServerSecurityException.class, errorTypeRepository.lookupErrorType(SERVER_SECURITY).get())
            .addExceptionMapping(NotPermittedException.class, errorTypeRepository.lookupErrorType(NOT_PERMITTED).get())
            .addExceptionMapping(RejectedExecutionException.class, errorTypeRepository.getErrorType(OVERLOAD).get())
            .addExceptionMapping(FlowBackPressureException.class, errorTypeRepository.getErrorType(FLOW_BACK_PRESSURE).get())
            .addExceptionMapping(MessageRedeliveredException.class,
                                 errorTypeRepository.lookupErrorType(REDELIVERY_EXHAUSTED).get())
            .addExceptionMapping(Exception.class, unknown)
            .addExceptionMapping(EventContextDeepNestingException.class, errorTypeRepository.getCriticalErrorType())
            .addExceptionMapping(Error.class, errorTypeRepository.getCriticalErrorType())
            .addExceptionMapping(StreamingBufferSizeExceededException.class,
                                 errorTypeRepository.lookupErrorType(STREAM_MAXIMUM_SIZE_EXCEEDED).get())
            .addExceptionMapping(MuleFatalException.class, errorTypeRepository.getErrorType(FATAL).get())
            .build())
        .defaultError(unknown)
        .build();
  }
}
