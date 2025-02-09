/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.exception;

import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.exception.MessagingException;

/**
 * Provides a set of utilities to work with {@link MessagingException} instances.
 *
 * @since 4.0
 */
public final class MessagingExceptionUtils {

  private MessagingExceptionUtils() {
    // Nothing to do
  }

  /**
   * Marks an exception as handled so it won't be re-thrown
   */
  public static void markAsHandled(Exception exception) {
    if (exception instanceof MessagingException) {
      ((MessagingException) exception).setHandled(true);
    }
  }

  /**
   * Provides a way through privileged API to build a MessagingException with the provided {@code event} and {@code cause}.
   * <p>
   * Keep in mind that in most cases this shouldn't be needed, and properly using {@link ErrorMessageAwareException} should be the
   * way to do things.
   *
   * @since 4.3.0, 4.2.2
   */
  public static EventProcessingException createMessagingException(CoreEvent event, Throwable cause) {
    return new MessagingException(event, cause);
  }
}
