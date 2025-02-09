/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.internal.transformer.AbstractDiscoverableTransformer;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Converts a {@link Map} to a {@link MultiMap}.
 *
 * @since 4.0
 */
public class MapToMultiMap extends AbstractDiscoverableTransformer {

  public MapToMultiMap() {
    registerSourceType(DataType.fromType(Map.class));
    setReturnDataType(DataType.fromType(MultiMap.class));
  }

  @Override
  protected Object doTransform(Object src, Charset enc) throws TransformerException {
    Map map = (Map) src;
    MultiMap multiMap = new MultiMap(map);
    return multiMap;
  }
}
