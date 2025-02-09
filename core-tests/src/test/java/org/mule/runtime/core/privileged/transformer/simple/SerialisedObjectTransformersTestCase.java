/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.transformer.simple;

import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.tck.core.transformer.AbstractTransformerTestCase;
import org.mule.tck.testmodels.fruit.Orange;

import org.apache.commons.lang3.SerializationUtils;

public class SerialisedObjectTransformersTestCase extends AbstractTransformerTestCase {

  private Orange testObject = new Orange(new Integer(4), new Double(14.3), "nice!");

  @Override
  public Transformer getTransformer() throws Exception {
    SerializableToByteArray transfromer = new SerializableToByteArray();
    ((MuleContextWithRegistry) muleContext).getRegistry().registerObject(String.valueOf(transfromer.hashCode()), transfromer);
    return transfromer;
  }

  @Override
  public Transformer getRoundTripTransformer() throws Exception {
    ByteArrayToSerializable transfromer = new ByteArrayToSerializable();
    ((MuleContextWithRegistry) muleContext).getRegistry().registerObject(String.valueOf(transfromer.hashCode()), transfromer);
    return transfromer;
  }

  @Override
  public Object getTestData() {
    return testObject;
  }

  @Override
  public Object getResultData() {
    return SerializationUtils.serialize(testObject);
  }

}
