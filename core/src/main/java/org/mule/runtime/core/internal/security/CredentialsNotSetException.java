/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.security;

import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.api.security.SecurityContext;
import org.mule.runtime.core.api.security.SecurityFilter;
import org.mule.runtime.api.security.UnauthorisedException;

/**
 * <code>CredentialsNotSetException</code> is thrown when user credentials cannot be obtained from the current message
 */
public class CredentialsNotSetException extends UnauthorisedException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -6271648179641734580L;

  public CredentialsNotSetException(I18nMessage message) {
    super(message);
  }

  public CredentialsNotSetException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public CredentialsNotSetException(CoreEvent event, SecurityContext context, SecurityFilter filter) {
    super(context, filter.getClass().getName(),
          event.getContext().getOriginatingLocation().getComponentIdentifier().getIdentifier().getNamespace());
  }
}
