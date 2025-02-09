/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.mule.runtime.core.api.object.AbstractObjectFactory;
import org.mule.runtime.core.api.object.AbstractObjectFactoryTestCase;

public class SingletonObjectFactoryTestCase extends AbstractObjectFactoryTestCase {

  @Override
  public AbstractObjectFactory getUninitialisedObjectFactory() {
    return new SingletonObjectFactory();
  }

  @Override
  public void testGetObjectClass() throws Exception {
    SingletonObjectFactory factory = (SingletonObjectFactory) getUninitialisedObjectFactory();
    factory.setObjectClass(Object.class);
    factory.initialise();

    assertEquals(Object.class, factory.getObjectClass());
  }

  @Override
  public void testGet() throws Exception {
    SingletonObjectFactory factory = (SingletonObjectFactory) getUninitialisedObjectFactory();
    factory.setObjectClass(Object.class);
    factory.initialise();

    assertSame(factory.getInstance(muleContext), factory.getInstance(muleContext));
  }

}
