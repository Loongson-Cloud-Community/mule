/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.tck.core.transformer.AbstractTransformerTestCase;

import java.util.HashMap;
import java.util.Map;

public class MapMultiMapTransformerTestCase extends AbstractTransformerTestCase {

  private static final String KEY_1 = "KEY1";
  private static final String KEY_2 = "KEY2";
  private static final String VALUE = "VALUE";

  private Object testObject = new Object();

  @Override
  public Transformer getTransformer() throws Exception {
    MapToMultiMap transformer = new MapToMultiMap();
    ((MuleContextWithRegistry) muleContext).getRegistry().registerObject(String.valueOf(transformer.hashCode()), transformer);
    return transformer;
  }

  @Override
  public Transformer getRoundTripTransformer() throws Exception {
    return null;
  }

  @Override
  public Object getTestData() {
    Map<String, Object> map = new HashMap<>();
    map.put(KEY_1, testObject);
    map.put(KEY_2, VALUE);
    return map;
  }

  @Override
  public Object getResultData() {
    MultiMap<String, Object> multiMap = new MultiMap<>();
    multiMap.put(KEY_1, testObject);
    multiMap.put(KEY_2, VALUE);
    return multiMap;
  }

}
