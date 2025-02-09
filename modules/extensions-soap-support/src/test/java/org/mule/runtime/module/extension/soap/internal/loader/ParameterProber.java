/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.soap.internal.loader;

class ParameterProber {

  private final String name;
  private final String defaultValue;
  private final Class type;
  private final boolean required;

  ParameterProber(String name, String defaultValue, Class type, boolean required) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.type = type;
    this.required = required;
  }

  ParameterProber(String name, Class type) {
    this(name, null, type, true);
  }

  public String getName() {
    return name;
  }

  public boolean isRequired() {
    return required;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Class getType() {
    return type;
  }
}
