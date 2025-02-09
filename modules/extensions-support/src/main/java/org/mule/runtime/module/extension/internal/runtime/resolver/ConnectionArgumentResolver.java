/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static java.lang.String.format;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.extension.api.util.NameUtils.getComponentModelTypeName;
import static org.mule.runtime.module.extension.internal.ExtensionProperties.CONNECTION_PARAM;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.extension.api.runtime.operation.ExecutionContext;
import org.mule.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import org.mule.runtime.module.extension.internal.ExtensionProperties;

/**
 * Returns the value of the {@link ExtensionProperties#CONNECTION_PARAM} variable, which is expected to have been previously set
 * on the supplied {@link ExecutionContext}.
 * <p>
 * Notice that for this to work, the {@link ExecutionContext} has to be an instance of {@link ExecutionContextAdapter}
 *
 * @since 4.0
 */
public class ConnectionArgumentResolver implements ArgumentResolver<Object> {

  /**
   * Returns the connection previously set on the {@code executionContext} under the key
   * {@link ExtensionProperties#CONNECTION_PARAM}
   *
   * @param executionContext an {@link ExecutionContext}
   * @return the connection
   * @throws IllegalArgumentException if the connection was not set
   * @throws ClassCastException       if {@code executionContext} is not an {@link ExecutionContextAdapter}
   */
  @Override
  public Object resolve(ExecutionContext executionContext) {
    ConnectionHandler connectionHandler =
        ((ExecutionContextAdapter<ComponentModel>) executionContext).getVariable(CONNECTION_PARAM);
    if (connectionHandler == null) {
      throw new IllegalArgumentException(format("No connection was provided for the component [%s]",
                                                executionContext.getComponentModel().getName()));
    }

    try {
      return connectionHandler.getConnection();
    } catch (ConnectionException e) {
      throw new MuleRuntimeException(
                                     createStaticMessage(format("Error was found trying to obtain a connection to execute %s '%s' of extension '%s'",
                                                                getComponentModelTypeName(executionContext.getComponentModel()),
                                                                executionContext.getComponentModel().getName(),
                                                                executionContext.getExtensionModel().getName())),
                                     e);
    }
  }
}
