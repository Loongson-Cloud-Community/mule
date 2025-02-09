/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.context.thread.notification;

import org.slf4j.Logger;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Allows to track {@link ThreadNotificationElement} to log Thread switches measured latency
 *
 * @since 4.2
 *
 * @deprecated since 4.4.0 this feature was removed
 */
@Deprecated
public interface ThreadNotificationService {

  String REGISTRY_KEY = "_muleThreadNotificationService";
  Logger REPORT_LOGGER = getLogger(ThreadNotificationService.class);

  /**
   * Takes a new {@link ThreadNotificationElement} to retrieve statistics
   *
   * @param notification
   *
   * @since 4.2
   */
  void addThreadNotificationElement(ThreadNotificationElement notification);

  /**
   * Takes a {@link Collection} of {@link ThreadNotificationElement} to retrieve all of their statistics.
   *
   * @param notifications
   */
  void addThreadNotificationElements(Collection<ThreadNotificationElement> notifications);

  /**
   * Retrieves stats notification as String. For each transition, it shows the total of transitions, the latency, the average and
   * standard deviation of this latency.
   *
   * @return string with retrieved statistics
   */
  String getNotification();

  /**
   * Clears all of the statistics retrieved.
   */
  void clear();

  interface ThreadNotificationElement {

    /**
     * @return the latency time for this thread switch
     */
    long getLatencyTime();

    /**
     * @return the starting thread pool type
     */
    String getFromThreadType();

    /**
     * @return the finishing thread pool type
     */
    String getToThreadType();

  }

}
