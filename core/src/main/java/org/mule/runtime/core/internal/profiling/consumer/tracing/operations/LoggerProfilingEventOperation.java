/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.profiling.consumer.tracing.operations;

import static org.mule.runtime.core.internal.profiling.consumer.ComponentProfilingUtils.getProcessingStrategyComponentInfoMap;

import org.mule.runtime.api.profiling.type.ProfilingEventType;
import org.mule.runtime.api.profiling.type.context.ComponentProcessingStrategyProfilingEventContext;

import com.google.gson.Gson;
import org.slf4j.Logger;

/**
 * A {@link ProfilingExecutionOperation} that logs the data of a profiling event.
 *
 * @since 4.5.0
 */
public class LoggerProfilingEventOperation implements
    ProfilingExecutionOperation<ComponentProcessingStrategyProfilingEventContext> {

  private final Logger logger;
  private final ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType;

  private final Gson gson = new Gson();

  public LoggerProfilingEventOperation(Logger logger,
                                       ProfilingEventType<ComponentProcessingStrategyProfilingEventContext> profilingEventType) {
    this.logger = logger;
    this.profilingEventType = profilingEventType;
  }

  @Override
  public void execute(ComponentProcessingStrategyProfilingEventContext profilingEventContext) {
    if (logger.isDebugEnabled()) {
      logger.debug(gson.toJson(getProcessingStrategyComponentInfoMap(profilingEventType, profilingEventContext)));
    }
  }
}
