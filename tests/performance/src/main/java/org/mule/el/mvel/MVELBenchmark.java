/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.el.mvel;

import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_EXPRESSION_LANGUAGE;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.mule.runtime.core.privileged.registry.LegacyRegistryUtils.lookupObject;

import org.mule.AbstractBenchmark;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.el.ExtendedExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.el.DefaultExpressionManager;
import org.mule.runtime.core.internal.el.mvel.MVELExpressionLanguage;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

public class MVELBenchmark extends AbstractBenchmark {

  final protected String mel = "mel:StringBuilder sb = new StringBuilder(); fields = payload.split(',\');"
      + "if (fields.length > 4) {"
      + "    sb.append('  <Contact>\n');"
      + "    sb.append('    <FirstName>').append(fields[0]).append('</FirstName>\n');"
      + "    sb.append('    <LastName>').append(fields[1]).append('</LastName>\n');"
      + "    sb.append('    <Address>').append(fields[2]).append('</Address>\n');"
      + "    sb.append('    <TelNum>').append(fields[3]).append('</TelNum>\n');"
      + "    sb.append('    <SIN>').append(fields[4]).append('</SIN>\n');"
      + "    sb.append('  </Contact>\n');" + "}" + "sb.toString();";

  final protected String payload = "Tom,Fennelly,Male,4,Ireland";

  private MuleContext muleContext;
  private Flow flow;
  private CoreEvent event;

  @Setup
  public void setup() throws MuleException {
    muleContext = createMuleContextWithServices();
    MVELExpressionLanguage mvelExpressionLanguage =
        (MVELExpressionLanguage) lookupObject(muleContext, OBJECT_EXPRESSION_LANGUAGE);
    mvelExpressionLanguage.setAutoResolveVariables(false);

    ExtendedExpressionManager expressionManager = muleContext.getExpressionManager();
    ((DefaultExpressionManager) expressionManager).setMelDefault(true);
    ((DefaultExpressionManager) expressionManager).setExpressionLanguage(mvelExpressionLanguage);

    flow = createFlow(muleContext);
    event = createEvent(flow);
  }

  @TearDown
  public void teardown() throws MuleException {
    stopIfNeeded(lookupObject(muleContext, SchedulerService.class));
    muleContext.dispose();
  }

  /**
   * Cold start: - New expression for each iteration - New context (message) for each iteration
   */
  @Benchmark
  public Object mvelColdStart() {
    return muleContext.getExpressionManager().evaluate(mel + new Random().nextInt(), createEvent(flow));
  }

  /**
   * Warm start: - Same expression for each iteration - New context (message) for each iteration
   */
  @Benchmark
  public Object mvelWarmStart() {
    return muleContext.getExpressionManager().evaluate(mel, event);
  }

  /**
   * Hot start: - Same expression for each iteration - Same context (message) for each iteration
   */
  @Benchmark
  public Object mvelHotStart() {
    return muleContext.getExpressionManager().evaluate(mel, event);
  }

}
