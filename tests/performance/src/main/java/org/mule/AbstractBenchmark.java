/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.mule.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.core.api.construct.Flow.builder;
import static org.mule.runtime.core.api.event.EventContextFactory.create;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsString;
import static org.mule.runtime.dsl.api.component.config.DefaultComponentLocation.from;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.api.config.builders.SimpleConfigurationBuilder;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.context.DefaultMuleContextFactory;
import org.mule.runtime.core.api.context.MuleContextFactory;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.config.builders.MinimalConfigurationBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Fork(1)
@Threads(1)
@BenchmarkMode(AverageTime)
@OutputTimeUnit(MICROSECONDS)
@State(Benchmark)
public class AbstractBenchmark {

  private final static Logger LOGGER = LoggerFactory.getLogger(AbstractBenchmark.class);

  public static final String CONNECTOR_NAME = "test";
  public static final String FLOW_NAME = "flow";
  public static final String PAYLOAD;
  public static final String KEY = "key";
  public static final String VALUE = "value";
  public static final ComponentLocation CONNECTOR_LOCATION = from(CONNECTOR_NAME);

  static {
    try {
      PAYLOAD = getResourceAsString("test-data.json", AbstractBenchmark.class);
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  protected MuleContext createMuleContextWithServices() throws MuleException {
    MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
    List<ConfigurationBuilder> builderList = new ArrayList<>();
    builderList.add(new SimpleConfigurationBuilder(getStartUpRegistryObjects()));
    builderList.add(new BasicRuntimeServicesConfigurationBuilder());
    builderList.add(new MinimalConfigurationBuilder());
    builderList.addAll(getAdditionalConfigurationBuilders());
    return muleContextFactory.createMuleContext(builderList.toArray(new ConfigurationBuilder[] {}));
  }

  protected Flow createFlow(MuleContext muleContext) {
    final Flow flow = builder(FLOW_NAME, muleContext).build();
    flow.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    return flow;
  }

  protected Map<String, Object> getStartUpRegistryObjects() {
    return new HashMap<>();
  }

  protected List<ConfigurationBuilder> getAdditionalConfigurationBuilders() {
    return emptyList();
  }

  public CoreEvent createEvent(Flow flow) {
    return createEvent(flow, PAYLOAD);
  }

  public CoreEvent createEvent(Flow flow, Object payload) {
    try {
      return CoreEvent.builder(create(flow, CONNECTOR_LOCATION)).message(of(payload)).build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
