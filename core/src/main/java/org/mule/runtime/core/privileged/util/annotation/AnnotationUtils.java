/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.util.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * A helper class for reading annotations.
 */
public class AnnotationUtils {

  public static List<AnnotationMetaData> getMethodAnnotations(Class<?> c, Class<? extends Annotation> ann) {
    return asList(c.getMethods()).stream().filter(method -> method.isAnnotationPresent(ann))
        .map(method -> new AnnotationMetaData(c, method, METHOD, method.getAnnotation(ann))).collect(toList());
  }

  public static <T extends Annotation> Optional<T> getAnnotation(Class<?> aClass, Class<T> annotationType) {
    return aClass != null ? ofNullable(aClass.getAnnotation(annotationType)) : empty();
  }

  private AnnotationUtils() {}
}
