/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.transformer.Converter.DEFAULT_PRIORITY_WEIGHTING;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Test;

/**
 * This unit test only tests the default priority of the {@link ObjectToByteArray} transformer. Actual transformation logic is
 * tested in the {@link ObjectByteArrayTransformersWithObjectsTestCase} test and its subclasses.
 */
@SmallTest
public class ObjectToByteArrayTestCase extends AbstractMuleTestCase {

  @Test
  public void transformerHasHigherDefaultPriority() throws Exception {
    ObjectToByteArray transformer = new ObjectToByteArray();
    assertThat(transformer.getPriorityWeighting(), equalTo(DEFAULT_PRIORITY_WEIGHTING + 1));
  }
}
