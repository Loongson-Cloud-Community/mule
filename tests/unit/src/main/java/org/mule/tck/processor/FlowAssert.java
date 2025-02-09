/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FlowAssert {

  private static Map<String, List<FlowAssertion>> assertions = new TreeMap<>();

  public static void verify() throws Exception {
    for (List<FlowAssertion> flowAssertions : assertions.values()) {
      for (FlowAssertion assertion : flowAssertions) {
        assertion.verify();
      }
    }
  }

  public static void verify(String flowName) throws Exception {

    List<FlowAssertion> flowAssertions = assertions.get(flowName);
    if (flowAssertions != null) {
      for (FlowAssertion assertion : flowAssertions) {
        assertion.verify();
      }
    }
  }

  public static void addAssertion(String flowName, FlowAssertion assertion) {
    synchronized (assertions) {
      if (assertions.get(flowName) == null) {
        assertions.put(flowName, new ArrayList<FlowAssertion>());
      }
      assertions.get(flowName).add(assertion);
    }
  }

  public static void reset() {
    assertions = new TreeMap<String, List<FlowAssertion>>();
  }

}
