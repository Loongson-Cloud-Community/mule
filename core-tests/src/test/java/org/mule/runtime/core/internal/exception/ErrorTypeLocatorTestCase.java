/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.exception;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.runtime.ast.api.error.ErrorTypeRepositoryProvider.getCoreErrorTypeRepo;
import static org.mule.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.UNKNOWN;
import static org.mule.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static org.mule.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.EXCEPTION_MAPPINGS;

import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.exception.ExceptionMapper;
import org.mule.runtime.core.privileged.exception.ErrorTypeLocator;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(ERROR_HANDLING)
@Story(EXCEPTION_MAPPINGS)
public class ErrorTypeLocatorTestCase extends AbstractMuleTestCase {

  private final ErrorTypeRepository repository = getCoreErrorTypeRepo();

  @Test
  public void useDefaultErrorWhenNoMappingFound() {
    ErrorType mockedError = mock(ErrorType.class);
    ErrorType unknown = repository.getErrorType(UNKNOWN).get();
    ErrorTypeLocator locator = ErrorTypeLocator.builder(repository)
        .defaultExceptionMapper(ExceptionMapper.builder().addExceptionMapping(Exception.class, mockedError).build())
        .defaultError(unknown)
        .build();

    ErrorType expectedError = locator.lookupErrorType(Exception.class);
    assertThat(expectedError, is(sameInstance(mockedError)));

    ErrorType defaultError = locator.lookupErrorType(Throwable.class);
    assertThat(defaultError, is(sameInstance(unknown)));
  }
}
