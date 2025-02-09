/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule;

import static java.util.Arrays.asList;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;

import java.util.List;
import java.util.Map.Entry;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;

public class MultiMapBenchmark extends AbstractBenchmark {

  private MultiMap<String, String> multiMap;

  @Setup
  public void setup() throws Exception {
    multiMap = new MultiMap<>();
    multiMap.put("key1", "value");
    multiMap.put("key10",
                 asList("value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8", "value9", "value10"));
  }

  @Benchmark
  public MultiMap<String, String> iteration() {
    MultiMap<String, String> targetMap = new MultiMap<>();

    targetMap.putAll(multiMap);

    return targetMap;
  }

  @Benchmark
  public List<Entry<String, String>> entryList() {
    return multiMap.entryList();
  }

  @Benchmark
  public MultiMap<String, String> copyWithKeySet() {
    MultiMap<String, String> targetMap = new MultiMap<>();

    for (String key : multiMap.keySet()) {
      targetMap.put(key, multiMap.getAll(key));
    }

    return targetMap;
  }

  @Benchmark
  public MultiMap<String, String> copyWithEntryList() {
    MultiMap<String, String> targetMap = new MultiMap<>();

    for (Entry<String, String> entry : multiMap.entryList()) {
      targetMap.put(entry.getKey(), entry.getValue());
    }

    return targetMap;
  }

  @Benchmark
  public CaseInsensitiveMultiMap copy() {
    return new CaseInsensitiveMultiMap(multiMap);
  }

  @Benchmark
  public CaseInsensitiveMultiMap putAll() {
    CaseInsensitiveMultiMap mm = new CaseInsensitiveMultiMap();
    mm.putAll(multiMap);
    return mm;
  }

}
