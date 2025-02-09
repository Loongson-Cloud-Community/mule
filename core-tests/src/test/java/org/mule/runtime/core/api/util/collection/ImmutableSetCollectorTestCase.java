/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util.collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.mule.runtime.internal.util.collection.ImmutableSetCollector;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;

@SmallTest
public class ImmutableSetCollectorTestCase extends AbstractMuleTestCase {

  @Test
  public void collect() {
    Set<String> collected = asList(new String[] {"a", "b", "a", "c"}).stream().collect(new ImmutableSetCollector<>());
    assertThat(collected, hasSize(3));
    assertThat(collected, containsInAnyOrder(new String[] {"a", "b", "c"}));
  }

  @Test
  public void emptySet() {
    Set<String> collected = new ArrayList<String>().stream().collect(new ImmutableSetCollector<>());
    assertThat(collected, is(notNullValue()));
    assertThat(collected, hasSize(0));
  }
}
