/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.domain;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.util.MultiMap.unmodifiableMultiMap;
import static org.mule.test.allure.AllureConstants.HttpFeature.HTTP_SERVICE;
import static org.mule.test.allure.AllureConstants.HttpFeature.HttpStory.MULTI_MAP;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.test.util.tck.MultiMapTestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_SERVICE)
@Story(MULTI_MAP)
public class CaseInsensitiveMultiMapTestCase extends MultiMapTestCase {

  public CaseInsensitiveMultiMapTestCase(Supplier<MultiMap<String, String>> mapSupplier,
                                         Function<MultiMap<String, String>, MultiMap<String, String>> mapCopier) {
    super(mapSupplier, mapCopier);
  }

  @Parameters
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {(Supplier<MultiMap<String, String>>) (() -> new CaseInsensitiveMultiMap(new MultiMap<>())),
            (Function<MultiMap<String, String>, MultiMap<String, String>>) (CaseInsensitiveMultiMap::new)}
    });
  }

  @Test
  public void takesParamMapEntries() {
    MultiMap<String, String> sensitiveMultiMap = new MultiMap<>();
    sensitiveMultiMap.put(KEY_1, VALUE_1);
    sensitiveMultiMap.put(KEY_2, VALUE_1);
    sensitiveMultiMap.put(KEY_2, VALUE_2);
    CaseInsensitiveMultiMap insensitiveMultiMap = new CaseInsensitiveMultiMap(sensitiveMultiMap);

    assertThat(insensitiveMultiMap.get(KEY_1), is(VALUE_1));
    assertThat(insensitiveMultiMap.get(KEY_1.toLowerCase()), is(VALUE_1));
    assertThat(insensitiveMultiMap.get(KEY_2), is(VALUE_1));
    assertThat(insensitiveMultiMap.get(KEY_2.toLowerCase()), is(VALUE_1));

    assertThat(insensitiveMultiMap.getAll(KEY_1), is(asList(VALUE_1)));
    assertThat(insensitiveMultiMap.getAll(KEY_1.toLowerCase()), is(asList(VALUE_1)));
    assertThat(insensitiveMultiMap.getAll(KEY_2), is(asList(VALUE_1, VALUE_2)));
    assertThat(insensitiveMultiMap.getAll(KEY_2.toLowerCase()), is(asList(VALUE_1, VALUE_2)));
  }

  @Test
  public void putAndGetCase() {
    assertThat(multiMap.put("kEy", VALUE_1), nullValue());
    assertThat(multiMap.get("KeY"), is(VALUE_1));
    assertThat(multiMap.get("kEy"), is(VALUE_1));
    assertThat(multiMap.getAll("key"), is(asList(VALUE_1)));
    assertThat(multiMap.getAll("KEY"), is(asList(VALUE_1)));
  }

  @Test
  public void aggregatesSameCaseKeys() {
    assertThat(multiMap.put("kEy", VALUE_1), nullValue());
    assertThat(multiMap.put("KeY", VALUE_2), is(VALUE_1));
    assertThat(multiMap.get("key"), is(VALUE_1));
    assertThat(multiMap.getAll("KEY"), is(asList(VALUE_1, VALUE_2)));
  }

  @Test
  public void immutableRemainsCaseInsensitive() {
    multiMap.put("wHaTeVeR", VALUE_1);

    assertThat(multiMap.toImmutableMultiMap().get("Whatever"), is(VALUE_1));
  }

  @Test
  public void unmodifiableRemainsCaseInsensitive() {
    multiMap.put("wHaTeVeR", VALUE_1);

    assertThat(unmodifiableMultiMap(multiMap).get("Whatever"), is(VALUE_1));
  }

  @Test
  public void emptyEquality() {
    CaseInsensitiveMultiMap otherMultiMap = new CaseInsensitiveMultiMap();

    assertThat(multiMap, is(equalTo(otherMultiMap)));
    assertThat(otherMultiMap, is(equalTo(multiMap)));
  }

  @Test
  public void complexEquality() {
    CaseInsensitiveMultiMap otherMultiMap = new CaseInsensitiveMultiMap();
    otherMultiMap.put("hello", "there");
    multiMap.put("hello", "there");
    otherMultiMap.put("hello", "stranger");
    multiMap.put("HellO", "stranger");
    otherMultiMap.put("bye", "dude");
    multiMap.put("BYE", "dude");

    assertThat(otherMultiMap, is(equalTo(multiMap)));
    assertThat(multiMap, is(equalTo(otherMultiMap)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void immutableCaseInsensitiveMultiMapFailsOnPut() {
    multiMap.toImmutableMultiMap().put(KEY_1, VALUE_1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void immutableCaseInsensitiveMultiMapFailsOnPutAll() {
    CaseInsensitiveMultiMap map = new CaseInsensitiveMultiMap();
    map.put(KEY_1, VALUE_1);
    multiMap.toImmutableMultiMap().putAll(map);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void immutableCaseInsensitiveMultiMapFailsOnRemove() {
    multiMap.toImmutableMultiMap().remove(KEY_1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void immutableCaseInsensitiveMultiMapFailsOnClear() {
    multiMap.toImmutableMultiMap().clear();
  }

  @Test
  public void toImmutableCaseInsensitiveMapKeepsOrder() {
    multiMap.put(KEY_3, VALUE_1);
    multiMap.put(KEY_2, VALUE_1);
    multiMap.put(KEY_1, VALUE_1);
    List<Entry<String, String>> entryList = multiMap.entryList();
    List<Entry<String, String>> immutableEntryList = multiMap.toImmutableMultiMap().entryList();
    assertEquals(entryList, immutableEntryList);
  }

}
