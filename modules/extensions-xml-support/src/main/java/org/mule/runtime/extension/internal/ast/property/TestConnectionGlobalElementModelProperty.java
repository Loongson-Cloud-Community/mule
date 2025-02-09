/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.extension.internal.ast.property;

import org.mule.runtime.api.meta.model.ModelProperty;

/**
 * Marker element to determine if any of the global elements of the current Smart Connector should be macro expanded holding the
 * original bean name from the application. By doing so, the most internal macro expanded element will be (always) a java SDK
 * component to which tooling could be able to execute test connection.
 *
 * @since 4.0
 */
public class TestConnectionGlobalElementModelProperty implements ModelProperty {

  private static final long serialVersionUID = 5298491194279550347L;

  private final String globalElementName;

  public TestConnectionGlobalElementModelProperty(String globalElementName) {
    this.globalElementName = globalElementName;
  }

  /**
   * @return The name of the global element to which the macro expansion must treat differently when macro expanding.
   */
  public String getGlobalElementName() {
    return globalElementName;
  }

  @Override
  public String getName() {
    return "testConnectionGlobalElementModelProperty";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
