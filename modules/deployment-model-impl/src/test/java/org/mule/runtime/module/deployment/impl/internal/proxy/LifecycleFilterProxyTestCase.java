/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.mule.runtime.module.deployment.impl.internal.policy.proxy.LifecycleFilterProxy.createLifecycleFilterProxy;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Before;
import org.junit.Test;

public class LifecycleFilterProxyTestCase extends AbstractMuleTestCase {

  private ProxiedObjectImplementation innerObject;
  private ProxiedObject proxiedObject;

  @Before
  public void setUp() {
    this.innerObject = new ProxiedObjectImplementation();
    this.proxiedObject = createLifecycleFilterProxy(innerObject);
  }

  @Test
  public void callSomeMethod() {
    proxiedObject.someMethod();
    assertThat(innerObject.someMethodExecuted, is(true));
    assertThat(proxiedObject, is(innerObject));
    assertFalse(proxiedObject == innerObject);
  }

  @Test
  public void startIsNotExecuted() throws MuleException {
    proxiedObject.start();
    assertThat(innerObject.startExecuted, is(false));
  }

  @Test
  public void stopIsNotExecuted() throws MuleException {
    proxiedObject.stop();
    assertThat(innerObject.stopExecuted, is(false));
  }

  @Test
  public void disposeIsNotExecuted() throws MuleException {
    proxiedObject.dispose();
    assertThat(innerObject.disposeExecuted, is(false));
  }

  class ProxiedObjectImplementation implements ProxiedObject {

    private boolean someMethodExecuted;
    private boolean disposeExecuted;
    private boolean startExecuted;
    private boolean stopExecuted;

    @Override
    public void someMethod() {
      this.someMethodExecuted = true;
    }

    @Override
    public void start() throws MuleException {
      this.startExecuted = true;
    }

    @Override
    public void stop() throws MuleException {
      this.stopExecuted = true;
    }

    @Override
    public void dispose() {
      this.disposeExecuted = true;
    }
  }

  public interface ProxiedObject extends Startable, Stoppable, Disposable {

    void someMethod();
  }
}
