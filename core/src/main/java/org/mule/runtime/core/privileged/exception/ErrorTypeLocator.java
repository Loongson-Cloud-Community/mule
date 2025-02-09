/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.exception;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.util.Optional.empty;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.api.util.Preconditions.checkState;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.core.api.exception.ExceptionMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * Locator for error types.
 *
 * The locator is responsible for getting the error type of any exception.
 *
 * An exception can be a general exception or an exception thrown by a configured component.
 *
 * There's a default mapping that will be used when there's no specific mapping between a component and the exception type thrown.
 *
 * To create an {@code ErrorTypeLocator} you must use the {@code Builder}. An instance of the builder can be created using the
 * static method {@code #builder}.
 *
 * @since 4.0
 */
@NoExtend
public class ErrorTypeLocator {

  private final ExceptionMapper defaultExceptionMapper;
  private final Map<ComponentIdentifier, ExceptionMapper> componentExceptionMappers;
  private final ErrorType defaultError;

  private final LoadingCache<Pair<ComponentIdentifier, Class<? extends Throwable>>, ErrorType> componentErrorTypeCache;

  protected ErrorTypeLocator(ExceptionMapper defaultExceptionMapper,
                             Map<ComponentIdentifier, ExceptionMapper> componentExceptionMappers,
                             ErrorType defaultError) {
    this.defaultExceptionMapper = defaultExceptionMapper;
    this.componentExceptionMappers = componentExceptionMappers;
    this.defaultError = defaultError;

    this.componentErrorTypeCache = newBuilder().build(params -> {
      ExceptionMapper exceptionMapper = componentExceptionMappers.get(params.getFirst());
      Optional<ErrorType> errorType = empty();
      if (exceptionMapper != null) {
        errorType = exceptionMapper.resolveErrorType(params.getSecond());
      }
      return errorType.orElseGet(() -> lookupErrorType(params.getSecond()));
    });
  }

  /**
   * Finds the {@code ErrorType} related to the provided {@code exception} based on the general mapping rules of the runtime.
   *
   * @param exception the exception related to the error type
   * @return the error type related to the exception. If there's no mapping then the error type related to UNKNOWN will be
   *         returned.
   */
  public ErrorType lookupErrorType(Throwable exception) {
    return lookupErrorType(exception.getClass());
  }

  /**
   * Finds the {@code ErrorType} related to the provided {@code exception} based on the general mapping rules of the runtime.
   *
   * @param type the exception {@link Class} related to the error type
   * @return the error type related to the exception. If there's no mapping then the error type related to UNKNOWN will be
   *         returned.
   */
  public ErrorType lookupErrorType(Class<? extends Throwable> type) {
    return defaultExceptionMapper.resolveErrorType(type).orElse(defaultError);
  }

  /**
   * Finds the {@code ErrorType} related to a component defined by the {@link ComponentIdentifier} based on the exception thrown
   * by the component and the mappings configured in the {@code ErrorTypeLocator}.
   * <p>
   * If no mapping is available then the {@link #lookupErrorType(Throwable)} rules applies.
   *
   * @param componentIdentifier the identifier of the component that throw the exception.
   * @param exception           the exception thrown by the component.
   * @return the error type related to the exception based on the component mappings. If there's no mapping then the error type
   *         related to UNKNOWN will be returned.
   */
  public ErrorType lookupComponentErrorType(ComponentIdentifier componentIdentifier, Class<? extends Throwable> exception) {
    return componentErrorTypeCache.get(new Pair<>(componentIdentifier, exception));
  }

  /**
   * Finds the {@code ErrorType} related to a component defined by the {@link ComponentIdentifier} based on the exception thrown
   * by the component and the mappings configured in the {@code ErrorTypeLocator}.
   * <p>
   * If no mapping is available then the {@link #lookupErrorType(Throwable)} rules applies.
   *
   * @param componentIdentifier the identifier of the component that throw the exception.
   * @param exception           the exception thrown by the component.
   * @return the error type related to the exception based on the component mappings. If there's no mapping then the error type
   *         related to UNKNOWN will be returned.
   */
  public ErrorType lookupComponentErrorType(ComponentIdentifier componentIdentifier, Throwable exception) {
    ErrorType errorTypeFromException = getErrorTypeFromException(exception);
    if (errorTypeFromException == null) {
      errorTypeFromException = lookupComponentErrorType(componentIdentifier, exception.getClass());
    }
    return errorTypeFromException;
  }

  private ErrorType getErrorTypeFromException(Throwable exception) {
    if (exception instanceof ConnectionException) {
      return ((ConnectionException) exception).getErrorType().orElse(null);
    } else {
      return null;
    }
  }

  /**
   * Adds an {@link ExceptionMapper} for a particular component identified by a {@link ComponentIdentifier}.
   *
   * @param componentIdentifier identifier of a component.
   * @param exceptionMapper     exception mapper for the component.
   */
  public void addComponentExceptionMapper(ComponentIdentifier componentIdentifier, ExceptionMapper exceptionMapper) {
    this.componentExceptionMappers.put(componentIdentifier, exceptionMapper);
  }

  /**
   * Builder for creating instances of {@link ErrorTypeLocator}.
   *
   * @param errorTypeRepository repository of error types.
   * @return a builder for creating an {@link ErrorTypeLocator}
   */
  public static Builder builder(ErrorTypeRepository errorTypeRepository) {
    return new Builder(errorTypeRepository);
  }

  /**
   * Builder for {@link ErrorTypeLocator}
   *
   * @since 4.0
   */
  public static class Builder {

    /**
     * Creates a builder instance.
     */
    public Builder(ErrorTypeRepository errorTypeRepository) {
      checkArgument(errorTypeRepository != null, "error type repository cannot be null");
    }

    private ErrorType defaultError;
    private ExceptionMapper defaultExceptionMapper;
    private final Map<ComponentIdentifier, ExceptionMapper> componentExceptionMappers = new HashMap<>();

    /**
     * Sets the default exception mapper to use when a component doesn't define a mapping for an exception type.
     *
     * @param exceptionMapper default exception mapper.
     * @return {@code this} builder.
     */
    public Builder defaultExceptionMapper(ExceptionMapper exceptionMapper) {
      this.defaultExceptionMapper = exceptionMapper;
      return this;
    }

    /**
     * Adds an {@link ExceptionMapper} for a particular component identified by the componentIdentifier.
     *
     * @param componentIdentifier identifier of a component.
     * @param exceptionMapper     exception mapper for the component.
     * @return {@code this} builder.
     */
    public Builder addComponentExceptionMapper(ComponentIdentifier componentIdentifier, ExceptionMapper exceptionMapper) {
      this.componentExceptionMappers.put(componentIdentifier, exceptionMapper);
      return this;
    }

    /**
     * Builds an {@link ErrorTypeLocator} instance with the provided configuration.
     *
     * @return an {@link ErrorTypeLocator} instance.
     */
    public ErrorTypeLocator build() {
      checkState(defaultExceptionMapper != null, "default exception mapper cannot not be null");
      checkState(componentExceptionMappers != null, "component exception mappers cannot not be null");
      checkState(defaultError != null, "default error cannot not be null");
      return new ErrorTypeLocator(defaultExceptionMapper, componentExceptionMappers, defaultError);
    }

    /**
     * Adds an {@link ErrorType} that is used when no mapping is found for a component.
     *
     * @return {@code this} builder.
     */
    public Builder defaultError(ErrorType defaultError) {
      this.defaultError = defaultError;
      return this;
    }
  }
}
