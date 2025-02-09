/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.transformer;

import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.IO_RW;

/**
 * Defines a {@link Transformer} that is a data type converters, ie: convert data from a type to another without modifying the
 * meaning of the data.
 */
public interface Converter extends Transformer {

  int MAX_PRIORITY_WEIGHTING = 10;
  int MIN_PRIORITY_WEIGHTING = 1;
  int DEFAULT_PRIORITY_WEIGHTING = MIN_PRIORITY_WEIGHTING;

  /**
   * If two or more discoverable transformers are equal, this value can be used to select the correct one
   *
   * @return the priority weighting for this transformer. This is a value between {@link #MIN_PRIORITY_WEIGHTING} and
   *         {@link #MAX_PRIORITY_WEIGHTING}.
   */
  int getPriorityWeighting();

  /**
   * If 2 or more discoverable transformers are equal, this value can be used to select the correct one
   *
   * @param weighting the priority weighting for this transformer. This is a value between {@link #MIN_PRIORITY_WEIGHTING} and
   *                  {@link #MAX_PRIORITY_WEIGHTING}.
   */
  void setPriorityWeighting(int weighting);

  @Override
  default ProcessingType getProcessingType() {
    if (getReturnDataType().isStreamType() || getSourceDataTypes().stream().anyMatch(dataType -> dataType.isStreamType())) {
      return IO_RW;
    } else {
      return CPU_LITE;
    }
  }

}
