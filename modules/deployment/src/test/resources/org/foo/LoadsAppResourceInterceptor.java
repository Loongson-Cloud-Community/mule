/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.foo;

import static java.lang.Thread.currentThread;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.interception.InterceptionEvent;
import org.mule.runtime.api.interception.ProcessorInterceptor;
import org.mule.runtime.api.interception.ProcessorParameterValue;

import java.util.Map;

public final class LoadsAppResourceInterceptor implements ProcessorInterceptor {

  @Override
  public void before(ComponentLocation location, Map<String, ProcessorParameterValue> parameters, InterceptionEvent event) {
    ClassLoader tccl = currentThread().getContextClassLoader();

    if (tccl.getResource("test-resource.txt") == null) {
      throw new AssertionError("Couldn't load exported resource");
    }
    if (tccl.getResource("test-resource-not-exported.txt") != null) {
      throw new AssertionError("Could load not exported resource");
    }

    try {
      tccl.loadClass("org.bar.BarUtils");
    } catch (ClassNotFoundException e) {
      throw new AssertionError("Couldn't load exported class", e);
    }
    try {
      tccl.loadClass("org.foo.EchoTest");
      throw new AssertionError("Could load not exported class");
    } catch (ClassNotFoundException e) {
      // expected
    }

  }
}