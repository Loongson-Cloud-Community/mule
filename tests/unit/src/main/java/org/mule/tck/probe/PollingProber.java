/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.probe;

import static org.mule.tck.report.ThreadDumper.logThreadDump;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.core.api.util.func.CheckedFunction;
import org.mule.runtime.core.api.util.func.CheckedSupplier;

import org.slf4j.Logger;

public class PollingProber implements Prober {

  private static final Logger LOGGER = getLogger(PollingProber.class);

  public static final long DEFAULT_TIMEOUT = 1000;
  public static final long DEFAULT_POLLING_INTERVAL = 100;

  private final long timeoutMillis;
  private final long pollDelayMillis;

  public static void check(long timeoutMillis, long pollDelayMillis, CheckedSupplier<Boolean> probe) {
    new PollingProber(timeoutMillis, pollDelayMillis).check(new JUnitLambdaProbe(probe));
  }

  /**
   * Similar to {@link #check(long, long, CheckedSupplier)} only that this one is expecting for the probe condition to
   * <b>NEVER</b> be met. If the condition is ever met, then an {@link AssertionError} is thrown
   */
  public static void checkNot(long timeoutMillis, long pollDelayMillis, CheckedSupplier<Boolean> probe) {
    try {
      check(timeoutMillis, pollDelayMillis, probe);
    } catch (AssertionError e) {
      return;
    }

    throw new AssertionError("Was expecting probe to fail");
  }

  public PollingProber() {
    this(DEFAULT_TIMEOUT, DEFAULT_POLLING_INTERVAL);
  }

  public PollingProber(long timeoutMillis, long pollDelayMillis) {
    this.timeoutMillis = timeoutMillis;
    this.pollDelayMillis = pollDelayMillis;
  }

  public static void probe(CheckedSupplier<Boolean> probable) {
    probe(probable, () -> null);
  }

  public static void probe(CheckedSupplier<Boolean> probable, CheckedSupplier<String> failureDescription) {
    new PollingProber().check(new JUnitLambdaProbe(probable, failureDescription));
  }

  public static void probe(CheckedSupplier<Boolean> probable, CheckedFunction<AssertionError, String> failureDescription) {
    new PollingProber().check(new JUnitLambdaProbe(probable, failureDescription));
  }

  public static void probe(long timeoutMillis, long pollDelayMillis, CheckedSupplier<Boolean> probable) {
    probe(timeoutMillis, pollDelayMillis, probable, () -> null);
  }

  public static void probe(long timeoutMillis, long pollDelayMillis, CheckedSupplier<Boolean> probable,
                           CheckedSupplier<String> failureDescription) {
    new PollingProber(timeoutMillis, pollDelayMillis).check(new JUnitLambdaProbe(probable, failureDescription));
  }

  public static void probe(long timeoutMillis, long pollDelayMillis, CheckedSupplier<Boolean> probable,
                           CheckedFunction<AssertionError, String> failureDescription) {
    new PollingProber(timeoutMillis, pollDelayMillis).check(new JUnitLambdaProbe(probable, failureDescription));
  }

  @Override
  public void check(Probe probe) {
    Probe jUnitProbe = JUnitProbeWrapper.wrap(probe);
    if (!poll(jUnitProbe)) {
      LOGGER.error("test timed out. Maybe due to a deadlock?");
      if (LOGGER.isTraceEnabled()) {
        logThreadDump();
      }
      throw new AssertionError(jUnitProbe.describeFailure());
    }
  }

  public boolean poll(Probe probe) {
    Timeout timeout = new Timeout(timeoutMillis);

    while (true) {
      if (probe.isSatisfied()) {
        return true;
      } else if (timeout.hasTimedOut()) {
        return false;
      } else {
        waitFor(pollDelayMillis);
      }
    }
  }

  private void waitFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      throw new IllegalStateException("unexpected interrupt", e);
    }
  }

  private static class JUnitProbeWrapper extends JUnitProbe {

    static JUnitProbe wrap(Probe probe) {
      if (probe instanceof JUnitProbe) {
        return (JUnitProbe) probe;
      } else {
        return new JUnitProbeWrapper(probe);
      }
    }

    private final Probe probe;

    private JUnitProbeWrapper(Probe probe) {
      super();
      this.probe = probe;
    }

    @Override
    protected boolean test() throws Exception {
      return probe.isSatisfied();
    }

    @Override
    public String describeFailure() {
      return probe.describeFailure();
    }
  }
}
