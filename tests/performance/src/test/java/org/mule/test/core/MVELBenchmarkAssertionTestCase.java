/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.core;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import org.mule.AbstractBenchmarkAssertionTestCase;
import org.mule.el.mvel.MVELBenchmark;
import org.mule.el.mvel.MVELDeepAssignBenchmark;
import org.mule.el.mvel.MVELDeepInvokeBenchmark;

import org.junit.Test;

public class MVELBenchmarkAssertionTestCase extends AbstractBenchmarkAssertionTestCase {

  @Test
  public void mvelColdStart() {
    runAndAssertBenchmark(MVELBenchmark.class, "mvelColdStart", 3000, MICROSECONDS);
  }

  @Test
  public void mvelWarmStart() {
    runAndAssertBenchmark(MVELBenchmark.class, "mvelWarmStart", 40, MICROSECONDS);
  }

  @Test
  public void mvelHotStart() {
    runAndAssertBenchmark(MVELBenchmark.class, "mvelHotStart", 30, MICROSECONDS);
  }

  @Test
  public void mvelColdStartDeepAssign() {
    runAndAssertBenchmark(MVELDeepAssignBenchmark.class, "mvelColdStart", 3000, MICROSECONDS);
  }

  @Test
  public void mvelWarmStartDeepAssign() {
    runAndAssertBenchmark(MVELDeepAssignBenchmark.class, "mvelWarmStart", 5, MICROSECONDS);
  }

  @Test
  public void mvelHotStartDeepAssign() {
    runAndAssertBenchmark(MVELDeepAssignBenchmark.class, "mvelHotStart", 5, MICROSECONDS);
  }

  @Test
  public void mvelColdStartDeepInvoke() {
    runAndAssertBenchmark(MVELDeepInvokeBenchmark.class, "mvelColdStart", 3000, MICROSECONDS);
  }

  @Test
  public void mvelWarmStartDeepInvoke() {
    runAndAssertBenchmark(MVELDeepInvokeBenchmark.class, "mvelWarmStart", 5, MICROSECONDS);
  }

  @Test
  public void mvelHotStartDeepInvoke() {
    runAndAssertBenchmark(MVELDeepInvokeBenchmark.class, "mvelHotStart", 5, MICROSECONDS);
  }

}
