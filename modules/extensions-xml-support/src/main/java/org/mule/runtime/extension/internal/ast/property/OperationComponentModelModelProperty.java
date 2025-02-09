/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.extension.internal.ast.property;

import org.mule.runtime.api.meta.model.ModelProperty;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.core.api.processor.Processor;

/**
 * Property to store the <operation/>'s {@link ComponentAst} and the <body/> that are contained in an extension written in XML.
 *
 * @since 4.0
 */
public class OperationComponentModelModelProperty implements ModelProperty {

  private static final long serialVersionUID = 3874939115159350906L;

  private final ComponentAst operationComponentModel;
  private final ComponentAst bodyComponentModel;

  /**
   * Constructs a {@link ModelProperty} that will hold the complete <operation/> and its {@link Processor}s defined in a <body/>
   * element to be later macro expanded into a Mule application.
   *
   * @param operationComponentModel <operation/> element represented through {@link ComponentAst}s.
   * @param bodyComponentModel      <body/> element with all the {@link Processor} represented through {@link ComponentAst}s.
   */
  public OperationComponentModelModelProperty(ComponentAst operationComponentModel, ComponentAst bodyComponentModel) {
    this.operationComponentModel = operationComponentModel;
    this.bodyComponentModel = bodyComponentModel;
  }

  /**
   * @return the {@link ComponentAst} that's pointing to the <operation/> element
   */
  public ComponentAst getOperationComponentModel() {
    return operationComponentModel;
  }

  /**
   * @return the {@link ComponentAst} that's pointing to the <body/> element
   */
  public ComponentAst getBodyComponentModel() {
    return bodyComponentModel;
  }

  @Override
  public String getName() {
    return "componentModelModelProperty";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
