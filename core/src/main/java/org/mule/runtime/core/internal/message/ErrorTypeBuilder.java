/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.message;

import static java.lang.String.format;
import static org.mule.runtime.api.util.Preconditions.checkState;
import static org.mule.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static org.mule.runtime.core.api.error.Errors.Identifiers.ANY_IDENTIFIER;
import static org.mule.runtime.core.api.error.Errors.Identifiers.CRITICAL_IDENTIFIER;

import org.mule.runtime.api.message.ErrorType;

import java.util.Objects;

/**
 * @deprecated Keeping this because the package it belongs to is exported. Use
 *             {@link org.mule.runtime.ast.internal.error.ErrorTypeBuilder} instead.
 */
@Deprecated
public class ErrorTypeBuilder {

  private String identifier;
  private String namespace;
  private ErrorType parentErrorType;

  public static ErrorTypeBuilder builder() {
    return new ErrorTypeBuilder();
  }

  private ErrorTypeBuilder() {}

  /**
   * Sets the error type identifier. @see {@link ErrorType#getIdentifier()}.
   *
   * The identifier must be unique within the same namespace.
   *
   * @param identifier the string representation
   * @return {@code this} builder
   */
  public ErrorTypeBuilder identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * Sets the error type namespace. @see {@link ErrorType#getNamespace()}
   *
   * @param namespace the error type namespace
   * @return {@code this} builder
   */
  public ErrorTypeBuilder namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Sets the parent error type. @see {@link ErrorType#getParentErrorType()}
   *
   * @param parentErrorType the parent error type
   * @return {@code this} builder
   */
  public ErrorTypeBuilder parentErrorType(ErrorType parentErrorType) {
    this.parentErrorType = parentErrorType;
    return this;
  }

  /**
   * Creates a new instance of the configured error type.
   *
   * @return the error type with the provided configuration.
   */
  public ErrorType build() {
    checkState(identifier != null, "string representation cannot be null");
    checkState(namespace != null, "namespace representation cannot be null");
    if (!isOrphan()) {
      checkState(parentErrorType != null, "parent error type cannot be null");
    }
    return new ErrorTypeImplementation(identifier, namespace, parentErrorType);
  }

  private boolean isOrphan() {
    return (identifier.equals(ANY_IDENTIFIER) || identifier.equals(CRITICAL_IDENTIFIER)) && namespace.equals(CORE_NAMESPACE_NAME);
  }

  /**
   * Default and only implementation of {@link ErrorType}
   */
  private final static class ErrorTypeImplementation implements ErrorType {

    private static final long serialVersionUID = -3716206147606234572L;

    private final String identifier;
    private final String namespace;
    private final ErrorType parentErrorType;

    private final String asString;

    private ErrorTypeImplementation(String identifier, String namespace, ErrorType parentErrorType) {
      this.identifier = identifier;
      this.namespace = namespace;
      this.parentErrorType = parentErrorType;

      this.asString = format("%s:%s", namespace, identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
      return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNamespace() {
      return namespace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorType getParentErrorType() {
      return parentErrorType;
    }

    @Override
    public String toString() {
      return asString;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ErrorTypeImplementation that = (ErrorTypeImplementation) o;
      return Objects.equals(identifier, that.identifier) &&
          Objects.equals(namespace, that.namespace) &&
          Objects.equals(parentErrorType, that.parentErrorType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(identifier, namespace, parentErrorType);
    }
  }

}
