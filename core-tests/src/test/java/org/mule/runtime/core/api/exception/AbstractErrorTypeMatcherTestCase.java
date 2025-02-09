/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.exception;

import static org.mule.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static org.mule.runtime.core.api.error.Errors.Identifiers.EXPRESSION_ERROR_IDENTIFIER;
import static org.mule.runtime.core.api.error.Errors.Identifiers.TRANSFORMATION_ERROR_IDENTIFIER;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.message.ErrorType;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Before;

public abstract class AbstractErrorTypeMatcherTestCase extends AbstractMuleContextTestCase {

  protected ErrorType anyErrorType;
  protected ErrorType transformationErrorType;
  protected ErrorType expressionErrorType;

  @Before
  public void setUp() {
    ErrorTypeRepository errorTypeRepository = muleContext.getErrorTypeRepository();
    anyErrorType = errorTypeRepository.getAnyErrorType();
    ComponentIdentifier transformationIdentifier =
        ComponentIdentifier.builder().name(TRANSFORMATION_ERROR_IDENTIFIER).namespace(CORE_NAMESPACE_NAME)
            .build();
    transformationErrorType = errorTypeRepository.lookupErrorType(transformationIdentifier).get();
    ComponentIdentifier expressionIdentifier =
        ComponentIdentifier.builder().name(EXPRESSION_ERROR_IDENTIFIER).namespace(CORE_NAMESPACE_NAME).build();
    expressionErrorType = errorTypeRepository.lookupErrorType(expressionIdentifier).get();
  }

}
