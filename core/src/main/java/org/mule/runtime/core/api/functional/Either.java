/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.functional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class allow to represent a type that holds two different values.
 *
 * Only one value can be present at any given type.
 *
 * This class can be used as a monad to interact and chain functions to be executed over the possible return values.
 *
 * Most likely the left type represent an error or failure result and the right value represent a successful result.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 *
 * @deprecated Use {@link org.mule.runtime.api.functional.Either} instead.
 */
@Deprecated
final public class Either<L, R> {

  /**
   * Creates an {@code Either} with a left value.
   *
   * @param value the left value
   * @param <L>   the left value type
   * @param <R>   the right value type
   * @return the created {@code Either instance}
   */
  public static <L, R> Either<L, R> left(L value) {
    return new Either<>(ofNullable(value), empty());
  }

  /**
   * Creates an {@code Either} with a left value and forcing a {@code class} for its empty right.
   * <p>
   * This is useful because it avoids the casting afterwards which can clutter the code and affect its readability.
   *
   * @param value the left value
   * @param <L>   the left value type
   * @param <R>   the right value type
   * @return the created {@code Either instance}
   */
  public static <L, R> Either<L, R> left(L value, Class<R> rightClass) {
    return new Either<>(ofNullable(value), empty());
  }

  /**
   * Creates an {@code Either} with a right value.
   *
   * @param value the right value
   * @param <L>   the left value type
   * @param <R>   the right value type
   * @return the created {@code Either instance}
   */
  public static <L, R> Either<L, R> right(R value) {
    return new Either<>(empty(), ofNullable(value));
  }

  /**
   * Creates an {@code Either} with a right value and forcing a {@code class} for its empty left.
   * <p>
   * This is useful because it avoids the casting afterwards which can clutter the code and affect its readability.
   *
   * @param value the right value
   * @param <L>   the left value type
   * @param <R>   the right value type
   * @return the created {@code Either instance}
   */
  public static <L, R> Either<L, R> right(Class<L> leftClass, R value) {
    return new Either<>(empty(), ofNullable(value));
  }

  private final Optional<L> left;
  private final Optional<R> right;

  private Either(Optional<L> l, Optional<R> r) {
    left = l;
    right = r;
  }

  /**
   * Allows to reduce to a single value using left and right functions with the same return type.
   *
   * @param leftFunc  the function to apply to the left value
   * @param rightFunc the function to apply to the left value
   * @param <T>       the return type of the function.
   * @return the result of applying the function on left of right values.
   */
  public <T> T reduce(Function<? super L, ? extends T> leftFunc, Function<? super R, ? extends T> rightFunc) {
    return isLeft() ? leftFunc.apply(left.get()) : rightFunc.apply(right.get());
  }

  /**
   * Allows to execute a function over the left value if it is present
   *
   * @param func the function to apply to the left value
   * @param <T>  the return type of the function.
   * @return a new {@code Either} created from the result of applying the function.
   */
  public <T> Either<T, R> mapLeft(Function<? super L, ? extends T> func) {
    return new Either<>(left.map(func), right);
  }

  public void applyLeft(Consumer<? super L> consumer) {
    left.ifPresent(consumer::accept);
  }

  public void applyRight(Consumer<? super R> consumer) {
    right.ifPresent(consumer::accept);
  }

  /**
   * Allows to execute a function over the right value if it is present
   *
   * @param func the function to apply to the right value
   * @param <T>  the return type of the function.
   * @return a new {@code Either} created from the result of applying the function.
   */
  public <T> Either<L, T> mapRight(Function<? super R, ? extends T> func) {
    return new Either<>(left, right.map(func));
  }

  /**
   * Receives a {@link Consumer} functions for both, the left and right value and applies the one over the value that is present.
   *
   * @param leftFunc  the function to apply to the left value
   * @param rightFunc the function to apply to the right value
   */
  public void apply(Consumer<? super L> leftFunc, Consumer<? super R> rightFunc) {
    applyLeft(leftFunc);
    applyRight(rightFunc);
  }

  /**
   * @return true if it holds a value for the left type, false otherwise
   */
  public boolean isLeft() {
    return left.isPresent();
  }

  /**
   * @return true if it holds a value for the right type, false otherwise
   */
  public boolean isRight() {
    return right.isPresent();
  }

  /**
   * @return the left value
   */
  public L getLeft() {
    return left.orElse(null);
  }

  /**
   * @return the right value
   */
  public R getRight() {
    return right.orElse(null);
  }

  public Optional<Object> getValue() {
    if (left.isPresent()) {
      return (Optional<Object>) left;
    } else if (right.isPresent()) {
      return (Optional<Object>) right;
    } else {
      return empty();
    }
  }

  @Override
  public String toString() {
    return format("%s - left: { %s }; right: { %s }", this.getClass().getSimpleName(), left.toString(), right.toString());
  }
}
