/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.datatype;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.internal.el.mvel.MessageVariableResolverFactory;
import org.mule.runtime.core.privileged.event.PrivilegedEvent;

/**
 * Propagates data type for session vars used for enrichment target
 */
public class SessionVarEnricherDataTypePropagator extends AbstractVariableEnricherDataTypePropagator {

  public SessionVarEnricherDataTypePropagator() {
    super(MessageVariableResolverFactory.SESSION_VARS);
  }

  @Override
  protected void addVariable(PrivilegedEvent event, PrivilegedEvent.Builder builder, TypedValue typedValue, String propertyName) {
    event.getSession().setProperty(propertyName, typedValue.getValue(), typedValue.getDataType());
  }

  @Override
  protected boolean containsVariable(PrivilegedEvent event, String propertyName) {
    return event.getSession().getPropertyNamesAsSet().contains(propertyName);
  }

}
