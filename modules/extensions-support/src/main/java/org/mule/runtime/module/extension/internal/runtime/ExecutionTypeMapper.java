/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime;

import static org.mule.runtime.api.meta.model.operation.ExecutionType.BLOCKING;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_LITE;
import org.mule.runtime.api.meta.model.operation.ExecutionType;
import org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType;

public final class ExecutionTypeMapper {

  private ExecutionTypeMapper() {}

  public static ProcessingType asProcessingType(ExecutionType executionType) {
    if (executionType == CPU_LITE) {
      return ProcessingType.CPU_LITE;
    } else if (executionType == BLOCKING) {
      return ProcessingType.BLOCKING;
    } else if (executionType == CPU_INTENSIVE) {
      return ProcessingType.CPU_INTENSIVE;
    } else {
      throw new IllegalArgumentException("Unsupported executionType " + executionType);
    }
  }
}
