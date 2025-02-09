/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.functional.api.component;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.StringUtils;

import javax.inject.Inject;

/**
 * Reads {@link CoreEvent} from a test connector's queue.
 */
public class QueueReaderMessageProcessor implements Processor {

  private final String queueName;
  private final Long timeout;
  private final TestConnectorQueueHandler queueHandler;

  @Inject
  private Registry registry;

  /**
   * Creates a queue reader
   *
   * @param registry  application's mule context. Not null.
   * @param queueName name of the queue to use. Non empty
   * @param timeout   number of milliseconds to wait for an available event. Non negative. Null means no timeout required.
   */
  public QueueReaderMessageProcessor(Registry registry, String queueName, Long timeout) {
    checkArgument(!StringUtils.isEmpty(queueName), "Queue name cannot be empty");
    if (timeout != null) {
      checkArgument(timeout >= 0L, "Timeout cannot be negative");
    }

    this.registry = registry;
    this.queueHandler = new TestConnectorQueueHandler(registry);
    this.queueName = queueName;
    this.timeout = timeout;
    this.registry = registry;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws MuleException {
    if (timeout == null) {
      return queueHandler.read(queueName);
    } else {
      return queueHandler.read(queueName, timeout);
    }
  }
}
