/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.source.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.MuleContext;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.concurrent.ScheduledFuture;

import org.junit.Test;

/**
 * Test to validate the interface {@link PeriodicScheduler} interface
 */
@SmallTest
public class PeriodicSchedulerTestCase extends AbstractMuleTestCase {

  /**
   * If the {@link Scheduler} created is null then throw an {@link ScheduledPollCreationException}
   */
  @Test(expected = NullPointerException.class)
  public void checkCreationOfNullScheduler() {
    factory(null, null).schedule(null, this.newRunnable());
  }

  private PeriodicScheduler factory(ScheduledFuture schedulerToReturn, MuleContext muleContext) {
    PeriodicScheduler pollFactory = spy(PeriodicScheduler.class);
    when(pollFactory.doSchedule(any(), any())).thenReturn(schedulerToReturn);
    return pollFactory;
  }

  private Runnable newRunnable() {
    return () -> {
      // no-op
    };
  }
}
