/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.testmodels.fruit;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.lifecycle.InitialisationCallback;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.object.ObjectFactory;

/**
 * <code>BananaFactory</code> is a test factory that creates Bananas
 */
public class BananaFactory implements ObjectFactory {

  @Override
  public void initialise() throws InitialisationException {
    // nothing to do
  }

  @Override
  public void dispose() {
    // nothing to do
  }

  @Override
  public Object getInstance(MuleContext muleContext) throws Exception {
    return new Banana();
  }

  @Override
  public Class<?> getObjectClass() {
    return Banana.class;
  }

  @Override
  public void addObjectInitialisationCallback(InitialisationCallback callback) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  @Override
  public boolean isExternallyManagedLifecycle() {
    return false;
  }

  @Override
  public boolean isAutoWireObject() {
    return false;
  }
}
