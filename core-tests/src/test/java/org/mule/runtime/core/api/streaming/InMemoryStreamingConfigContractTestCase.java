/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming;

import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.qameta.allure.Description;

public abstract class InMemoryStreamingConfigContractTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  @Description("Test that a valid config is indeed valid")
  public void initialSizeEqualsMaxSizeWithNoExpansion() {
    assertValid(1, 0, 1);
  }

  @Test
  @Description("initial size of zero should be invalid")
  public void zeroInitialSize() {
    assertInvalid(0, 1, 10);
  }

  @Test
  @Description("initial size cannot be negative")
  public void negativeInitialSIze() {
    assertInvalid(-1, 1, 10);
  }

  @Test
  @Description("increment size cannot be negative")
  public void negativeIncrementSize() {
    assertInvalid(10, -1, 20);
  }

  @Test
  @Description("initial size cannot be bigger the maxSize")
  public void initialSizeBiggerThanMaxSize() {
    assertInvalid(100, 1, 10);
  }

  @Test
  @Description("incrementSize cannot be bigger than the maxSize")
  public void incrementSizeBiggerThanMaxSize() {
    assertInvalid(10, 100, 20);
  }

  @Test
  @Description("initialSize + incrementSize cannot be bigger than maxSize")
  public void invalidExpansionSize() {
    assertInvalid(1, 100, 100);
  }

  private void assertValid(int initialSize, int increment, int maxSize) {
    createConfig(initialSize, increment, maxSize);
  }

  private void assertInvalid(int initialSize, int increment, int maxSize) {
    expectedException.expect(IllegalArgumentException.class);
    createConfig(initialSize, increment, maxSize);
  }

  protected abstract void createConfig(int initialSize, int increment, int maxSize);
}
