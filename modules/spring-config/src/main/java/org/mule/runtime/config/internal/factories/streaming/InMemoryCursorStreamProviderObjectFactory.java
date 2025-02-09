/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.factories.streaming;

import org.mule.runtime.api.util.DataSize;
import org.mule.runtime.api.util.DataUnit;
import org.mule.runtime.config.api.factories.streaming.AbstractCursorProviderObjectFactory;
import org.mule.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;

public class InMemoryCursorStreamProviderObjectFactory
    extends AbstractCursorProviderObjectFactory<CursorStreamProviderFactory> {

  private final int initialBufferSize;
  private final int bufferSizeIncrement;
  private final int maxInMemorySize;
  private DataUnit dataUnit;

  public InMemoryCursorStreamProviderObjectFactory(int initialBufferSize, int bufferSizeIncrement, int maxInMemorySize,
                                                   DataUnit dataUnit) {
    this.initialBufferSize = initialBufferSize;
    this.bufferSizeIncrement = bufferSizeIncrement;
    this.maxInMemorySize = maxInMemorySize;
    this.dataUnit = dataUnit;
  }

  @Override
  public CursorStreamProviderFactory doGetObject() throws Exception {
    InMemoryCursorStreamConfig config = new InMemoryCursorStreamConfig(new DataSize(initialBufferSize, dataUnit),
                                                                       new DataSize(bufferSizeIncrement, dataUnit),
                                                                       new DataSize(maxInMemorySize, dataUnit));


    return streamingManager.forBytes().getInMemoryCursorProviderFactory(config);
  }
}
