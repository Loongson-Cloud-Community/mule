/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.datatype;

import org.mule.mvel2.ast.ASTNode;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.privileged.event.PrivilegedEvent;

/**
 * Resolves data type for expressions representing a reference to a invocation or session variable.
 */
public class PropertyExpressionDataTypeResolver extends AbstractExpressionDataTypeResolver {

  @Override
  protected DataType getDataType(PrivilegedEvent event, ASTNode node) {
    if (node.isIdentifier() && event.getVariables().containsKey(node.getName())) {
      return event.getVariables().get(node.getName()).getDataType();
    } else if (node.isIdentifier() && event.getSession().getPropertyNamesAsSet().contains(node.getName())) {
      return event.getSession().getPropertyDataType(node.getName());
    } else {
      return null;
    }
  }
}
