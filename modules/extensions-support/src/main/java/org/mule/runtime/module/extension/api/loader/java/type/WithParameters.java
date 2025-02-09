/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.api.loader.java.type;

import org.mule.api.annotation.NoImplement;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A generic contract for any kind of component from which, a list of parameters can be derived
 *
 * @since 4.0
 */
@NoImplement
public interface WithParameters {

  /**
   * @return A list of {@link ExtensionParameter} that represents the parameters of the component
   */
  List<ExtensionParameter> getParameters();

  /**
   * @return A list of {@link ExtensionParameter} that represents the parameters of the component that are considered as parameter
   *         groups
   */
  List<ExtensionParameter> getParameterGroups();

  /**
   * @param annotationClass {@link Annotation} to look for parameters annotated with this class
   * @return A list of {@link ExtensionParameter} that are annotated with the given Annotation Class
   */
  List<ExtensionParameter> getParametersAnnotatedWith(Class<? extends Annotation> annotationClass);

}
