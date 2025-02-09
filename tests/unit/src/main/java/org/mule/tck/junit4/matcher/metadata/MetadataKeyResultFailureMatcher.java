/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.junit4.matcher.metadata;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import org.mule.runtime.api.metadata.resolving.MetadataFailure;
import org.mule.runtime.api.metadata.resolving.MetadataResult;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Verifies that a {@link MetadataResult} is not successful.
 *
 * @param <T> return type of the Metadata resolving operation.
 *
 * @since 4.3.0
 */
public class MetadataKeyResultFailureMatcher<T> extends TypeSafeMatcher<MetadataResult<T>> {

  private final Matcher<Collection<? extends MetadataFailure>> failuresMatcher;

  private MetadataResult<T> item;

  public MetadataKeyResultFailureMatcher(Matcher<Collection<? extends MetadataFailure>> failuresMatcher) {
    this.failuresMatcher = failuresMatcher;
  }

  @Override
  public void describeTo(Description description) {
    failuresMatcher.describeTo(description);
  }

  @Override
  protected boolean matchesSafely(MetadataResult<T> item) {
    this.item = item;
    return !item.isSuccess() && failuresMatcher.matches(item.getFailures());
  }

  public static <T> MetadataKeyResultFailureMatcher<T> isFailure() {
    return new MetadataKeyResultFailureMatcher<>(not(empty()));
  }

  public static <T> MetadataKeyResultFailureMatcher<T> isFailure(Matcher<Collection<? extends MetadataFailure>> failuresMatcher) {
    return new MetadataKeyResultFailureMatcher<>(failuresMatcher);
  }
}
