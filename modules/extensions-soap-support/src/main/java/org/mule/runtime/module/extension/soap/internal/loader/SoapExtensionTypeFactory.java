/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.soap.internal.loader;

import org.mule.metadata.api.ClassTypeLoader;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.module.extension.api.loader.java.type.ExtensionElement;
import org.mule.runtime.module.extension.soap.internal.loader.type.runtime.SoapExtensionTypeWrapper;

/**
 * Method Factory Pattern implementation that creates {@link ExtensionElement} for a Soap Extension from a {@link Class} that uses
 * the Soap Extensions API.
 *
 * @since 4.0
 */
class SoapExtensionTypeFactory {

  /**
   * Creates a {@link ExtensionElement} from a given {@link Class} that will help to introspect an extension.
   *
   * @param extensionType SOAP A class annotated with {@link Extension} or {@link org.mule.sdk.api.annotation.Extension}
   * @param typeLoader    a {@link ClassTypeLoader}
   * @return an {@link ExtensionElement} wrapping the extension {@link Class} structure
   */

  static SoapExtensionTypeWrapper getSoapExtensionType(Class<?> extensionType, ClassTypeLoader typeLoader) {
    return new SoapExtensionTypeWrapper<>(extensionType, typeLoader);
  }
}
