/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.mule.runtime.internal.util.collection.ImmutableMapCollector;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.tck.testmodels.fruit.Apple;
import org.mule.tck.testmodels.fruit.Banana;
import org.mule.tck.testmodels.fruit.Fruit;
import org.mule.tck.testmodels.fruit.Kiwi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SmallTest
public class ImmutableMapCollectorTestCase extends AbstractMuleTestCase {

  private final ImmutableMapCollector<Fruit, String, Fruit> collector =
      new ImmutableMapCollector<>(f -> f.getClass().getName(), f -> f);

  @Test
  public void collect() {
    final List<Fruit> fruits = Arrays.asList(new Apple(), new Banana(), new Kiwi());
    Map<String, Fruit> map = fruits.stream().collect(collector);

    assertThat(map.size(), is(3));
    fruits.forEach(fruit -> {
      Fruit value = map.get(fruit.getClass().getName());
      assertThat(value, sameInstance(fruit));
    });
  }

  @Test
  public void emptyMap() {
    Map<String, Fruit> map = new ArrayList<Fruit>().stream().collect(collector);
    assertThat(map.isEmpty(), is(true));
  }
}
