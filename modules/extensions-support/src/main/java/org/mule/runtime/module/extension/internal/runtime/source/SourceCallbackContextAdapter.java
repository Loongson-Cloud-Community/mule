/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.source;

import org.mule.runtime.core.internal.execution.NotificationFunction;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;

import java.util.List;

/**
 * Augments the {@link SourceCallbackContext} contract with internal behavior we don't want exposed on the public API
 *
 * @since 4.0
 */
public interface SourceCallbackContextAdapter extends SourceCallbackContext {

  /**
   * Releases the bound connection
   */
  void releaseConnection();

  /**
   * Indicates that {@code this} instance has already been used to dispatch an event
   *
   * @since 1.1
   */
  void dispatched();

  /**
   * Retrieves the notification functions.
   *
   * @return a list of {@link NotificationFunction NotificationFunctions} to evaluate and fire
   */
  List<NotificationFunction> getNotificationsFunctions();
}
