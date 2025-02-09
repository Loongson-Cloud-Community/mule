/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util.queue;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;

import java.util.Optional;

/**
 * A Queue manager is responsible for managing one or more Queue resources and providing common support for transactions and
 * persistence.
 */
@NoImplement
public interface QueueManager extends Startable, Stoppable {

  /**
   * Returns a new instance of {@link QueueSession} bounded to this {@link QueueManager}
   *
   * @return session for retrieving queues and handle transactions
   */
  QueueSession getQueueSession();

  /**
   * Sets the default {@link QueueConfiguration} for any created {@link Queue} for which a custom configuration hasn't been
   * specified
   *
   * @param config an instance of {@link QueueConfiguration}
   */
  void setDefaultQueueConfiguration(QueueConfiguration config);

  /**
   * Specifies a {@link QueueConfiguration} for the queue which name matches queueName
   *
   * @param queueName the name of a {@link Queue}
   * @param config    an instance of {@link QueueConfiguration}
   */
  void setQueueConfiguration(String queueName, QueueConfiguration config);

  /**
   * Returns the configuration of the {@link Queue} of the given {@code queueName}
   *
   * @param queueName the name of a configured queue
   * @return the queue's configuration or {@link Optional#empty()} if no such queue
   */
  Optional<QueueConfiguration> getQueueConfiguration(String queueName);
}
