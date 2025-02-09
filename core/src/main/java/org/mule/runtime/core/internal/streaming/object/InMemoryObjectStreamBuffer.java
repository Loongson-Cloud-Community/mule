/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.streaming.object;

import static java.lang.Math.floor;

import org.mule.runtime.api.streaming.exception.StreamingBufferSizeExceededException;
import org.mule.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * An {@link AbstractObjectStreamBuffer} implementation which uses buckets for locating items.
 *
 * @param <T> The generic type of the items in the stream
 * @sice 4.0
 */
public class InMemoryObjectStreamBuffer<T> extends AbstractObjectStreamBuffer<T> {

  private final InMemoryCursorIteratorConfig config;
  private List<Bucket<T>> buckets;


  public InMemoryObjectStreamBuffer(Iterator<T> stream, InMemoryCursorIteratorConfig config) {
    super(stream);
    this.config = config;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doClose() {
    buckets.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Position toPosition(long position) {
    int initialBufferSize = config.getInitialBufferSize();
    int bucketsDelta = config.getBufferSizeIncrement();

    if (position < initialBufferSize || bucketsDelta == 0) {
      return new Position(0, (int) position);
    }

    long offset = position - initialBufferSize;

    int bucketIndex = (int) floor(offset / bucketsDelta) + 1;
    int itemIndex = (int) position - (initialBufferSize + ((bucketIndex - 1) * bucketsDelta));

    return new Position(bucketIndex, itemIndex);
  }

  @Override
  protected void initialize(Optional<Position> maxPosition, Bucket<T> initialBucket) {
    buckets = maxPosition.map(p -> new ArrayList<Bucket<T>>(p.getBucketIndex())).orElseGet(ArrayList::new);
    initialBucket = new Bucket<>(0, config.getInitialBufferSize());
    buckets.add(initialBucket);
    setCurrentBucket(initialBucket);
  }

  @Override
  protected Bucket<T> getPresentBucket(Position position) {
    if (position.getBucketIndex() < buckets.size()) {
      return buckets.get(position.getBucketIndex());
    }

    return null;
  }

  @Override
  protected Bucket<T> onBucketOverflow(Bucket<T> overflownBucket) {
    Bucket<T> newBucket = new Bucket<>(overflownBucket.getIndex() + 1, config.getBufferSizeIncrement());
    buckets.add(newBucket);

    return newBucket;
  }

  @Override
  protected void validateMaxBufferSizeNotExceeded(int instancesCount) {
    if (instancesCount > config.getMaxBufferSize()) {
      throw new StreamingBufferSizeExceededException(config.getMaxBufferSize());
    }
  }
}
