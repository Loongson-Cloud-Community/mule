/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.execution;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.exception.MuleExceptionInfo;
import org.mule.runtime.api.notification.EnrichedNotificationInfo;

import java.util.Map;

/**
 * Provides a callback to add info entries to an exception just before logging/handling it.
 *
 * When an exception is thrown in a message processor, implementations of this interface will be called in order to augment the
 * exception message with properties that can be helpful to an application developer troubleshooting that exception.
 *
 * @since 3.8.0
 */
@NoImplement
public interface ExceptionContextProvider {

  /**
   * @param notificationInfo
   * @return info entries to be added to the logged exception message
   *
   * @deprecated Use {@link #putContextInfo(MuleExceptionInfo, EnrichedNotificationInfo, Component)} instead.
   */
  @Deprecated
  Map<String, Object> getContextInfo(EnrichedNotificationInfo notificationInfo, Component lastProcessed);

  /**
   * @param info             the map to put the entries to be added to the logged exception message into
   * @param notificationInfo
   * @param lastProcessed
   *
   * @since 4.3
   */
  void putContextInfo(MuleExceptionInfo info, EnrichedNotificationInfo notificationInfo, Component lastProcessed);

}
