/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming.iterator;


import java.io.IOException;

/**
 * Implementation of {@link StreamingIterator} that takes its elements from a {@link Consumer}.
 * <p>
 * Closing this iterator will cause the underlying consumer to be closed. If for any reason the underlying consumer gets closed
 * (either because this iterator closed it or some other reason), then this iterator will consider that it has no more items.
 * <p>
 *
 * @since 4.0
 */
public final class ConsumerStreamingIterator<T> implements StreamingIterator<T> {

  private Consumer<T> consumer;

  public ConsumerStreamingIterator(Consumer<T> consumer) {
    this.consumer = consumer;
  }

  /**
   * Closes the underlying consumer
   */
  @Override
  public void close() throws IOException {
    this.consumer.close();
  }

  /**
   * Returns true as long as the underlying consumer is not fully consumed nor closed
   */
  @Override
  public boolean hasNext() {
    return !this.consumer.isConsumed();
  }

  /**
   * Gets an item from the consumer and returns it
   */
  @Override
  public T next() {
    return this.consumer.consume();
  }

  @Override
  public int getSize() {
    return this.consumer.getSize();
  }

}
