/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.component;

import org.mule.runtime.core.privileged.component.AnnotatedObjectInvocationHandler;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Marker interface used by {@link AnnotatedObjectInvocationHandler} to identify any classes created by it.
 * <p>
 * Declares {@link #writeReplace()} in order to override default serialization mechanism.
 *
 * @since 1.0
 */
public interface DynamicallySerializableComponent extends DynamicallyComponent, Serializable {

  /**
   * Changes the object to actually be serialized when this is serialized.
   * <p>
   * This method is declared so that the Dynamic Class Builder (ByteBuddy) can intercept it.
   * 
   * @see Serializable
   * @see <a href=
   *      "https://github.com/cglib/cglib/wiki/How-To#cglib-and-java-serialization">https://github.com/cglib/cglib/wiki/How-To#cglib-and-java-serialization<a>
   */
  Object writeReplace() throws ObjectStreamException;
}
