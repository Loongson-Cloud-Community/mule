/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.resources.manifest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessingEnvironmentClassPackageFinderTestCase {

  private ProcessingEnvironmentClassPackageFinder processingEnvironmentClassPackageFinder;

  @Rule
  public CompilationRule compilationRule = new CompilationRule();

  @Before
  public void setUp() {
    ProcessingEnvironment processingEnvironment = mock(ProcessingEnvironment.class);
    when(processingEnvironment.getTypeUtils()).thenReturn(compilationRule.getTypes());
    when(processingEnvironment.getElementUtils()).thenReturn(compilationRule.getElements());

    processingEnvironmentClassPackageFinder = new ProcessingEnvironmentClassPackageFinder(processingEnvironment);
  }

  @Test
  public void packageForUsingProcessingEnvironment() {
    Optional<String> optionalPackage = processingEnvironmentClassPackageFinder
        .packageFor("org.mule.test.heisenberg.extension.model.hidingPlaces.Places.HidingPlaces");
    assertThat(optionalPackage.get(), is("org.mule.test.heisenberg.extension.model.hidingPlaces"));
  }

}
