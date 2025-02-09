/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.routing;

import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.api.i18n.I18nMessage;

/**
 * <code>CouldNotRouteOutboundMessageException</code> thrown if Mule fails to route the current outbound event.
 */

public final class CouldNotRouteOutboundMessageException extends RoutingException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 4609966704030524485L;

  public CouldNotRouteOutboundMessageException(Processor target) {
    super(target);
  }

  public CouldNotRouteOutboundMessageException(Processor target, Throwable cause) {
    super(target, cause);
  }

  public CouldNotRouteOutboundMessageException(I18nMessage message, Processor target) {
    super(message, target);
  }

  public CouldNotRouteOutboundMessageException(I18nMessage message, Processor target, Throwable cause) {
    super(message, target, cause);
  }
}
