/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.objectbuilder;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import org.mule.runtime.api.exception.MuleRuntimeException;

/**
 * Utilities to power {@link ObjectBuilder} implementations
 *
 * @since 4.0
 */
class ObjectBuilderUtils {

  /**
   * Creates a new instance of the given {@code prototypeClass}
   *
   * @param prototypeClass the class of the object to create
   * @param <T>            the generic type of the {@code prototypeClass}
   * @return a new instance
   */
  public static <T> T createInstance(Class<T> prototypeClass) {
    try {
      return prototypeClass.newInstance();
    } catch (Exception e) {
      throw new MuleRuntimeException(createStaticMessage("Could not create instance of " + prototypeClass), e);
    }
  }

  private ObjectBuilderUtils() {}
}
