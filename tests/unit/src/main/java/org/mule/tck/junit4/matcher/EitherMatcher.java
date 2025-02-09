/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.junit4.matcher;

import static org.hamcrest.Matchers.nullValue;

import org.mule.runtime.api.functional.Either;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * {@link Matcher} for {@link Either} instances.
 *
 * @since 4.0
 */
public class EitherMatcher<L, R> extends TypeSafeMatcher<Either<L, R>> {

  private final Matcher<L> leftMatcher;
  private final Matcher<R> rightMatcher;

  public EitherMatcher(Matcher<L> leftMatcher, Matcher<R> rightMatcher) {
    this.leftMatcher = leftMatcher;
    this.rightMatcher = rightMatcher;
  }

  public static <L, R> EitherMatcher<L, R> leftMatches(Matcher<L> matcher) {
    return new EitherMatcher(matcher, nullValue());
  }

  public static <L, R> EitherMatcher<L, R> rightMatches(Matcher<R> matcher) {
    return new EitherMatcher(nullValue(), matcher);
  }

  @Override
  protected boolean matchesSafely(Either<L, R> item) {
    return leftMatcher.matches(item.getLeft()) && rightMatcher.matches(item.getRight());
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("left is ");
    leftMatcher.describeTo(description);
    description.appendText("; and right is ");
    rightMatcher.describeTo(description);
  }

}
