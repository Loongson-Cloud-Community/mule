/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.queue;

import static java.lang.String.format;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract queue delegate implementation that forces common queue behaviour
 */
public abstract class AbstractQueueStoreDelegate implements QueueStoreDelegate {

  private final Logger logger = getLogger(AbstractQueueStoreDelegate.class);
  private final int capacity;

  public AbstractQueueStoreDelegate(int capacity) {
    this.capacity = capacity;
  }

  @Override
  public final void putNow(Serializable o) {
    synchronized (this) {
      add(o);
      this.notifyAll();
    }
  }

  @Override
  public final boolean offer(Serializable o, int room, long timeout) throws InterruptedException {
    checkInterrupted();
    synchronized (this) {
      if (capacity > 0) {
        if (capacity <= room) {
          throw new IllegalStateException("Can not add more objects than the capacity in one time");
        }
        long l1 = timeout > 0L ? System.currentTimeMillis() : 0L;
        long l2 = timeout;
        while (getSize() >= capacity - room) {
          if (timeout < 0) {
            // If timeout is negative then wait until notified without a
            // timeout.
            this.wait(0);
          } else {
            if (l2 <= 0L) {
              logger
                  .warn(format("Timeout of %d milliseconds reached, object could not be queued. Queue capacity of %d full.",
                               timeout, capacity));
              return false;
            }
            this.wait(l2);
            l2 = timeout - (System.currentTimeMillis() - l1);
          }
        }
      }
      if (o != null) {
        add(o);
      }
      this.notifyAll();
      return true;
    }
  }

  @Override
  public final Serializable poll(long timeout) throws InterruptedException {
    checkInterrupted();
    synchronized (this) {
      long l1 = timeout > 0L ? System.currentTimeMillis() : 0L;
      long l2 = timeout;
      while (isEmpty()) {
        if (l2 <= 0L) {
          return null;
        }
        this.wait(l2);
        l2 = timeout - (System.currentTimeMillis() - l1);
      }

      Serializable o = removeFirst();
      this.notifyAll();
      return o;
    }
  }

  @Override
  public final Serializable peek() throws InterruptedException {
    checkInterrupted();
    synchronized (this) {
      if (isEmpty()) {
        return null;
      } else {
        return getFirst();
      }
    }
  }

  @Override
  public final void untake(Serializable item) throws InterruptedException {
    checkInterrupted();
    synchronized (this) {
      addFirst(item);
      this.notifyAll();
    }
  }

  @Override
  public final void clear() throws InterruptedException {
    this.checkInterrupted();
    synchronized (this) {
      doClear();
    }
  }

  @Override
  public final boolean addAll(Collection<? extends Serializable> items) {
    synchronized (this) {
      boolean result = doAddAll(items);
      this.notifyAll();
      return result;
    }
  }

  private void checkInterrupted() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
  }

  @Override
  public int getSize() {
    synchronized (this) {
      return size();
    }
  }

  /**
   * @return the number of elements in the queue
   */
  protected abstract int size();

  /**
   * reads the first element in the queue
   *
   * @return the fist element in the queue
   * @throws InterruptedException
   */
  protected abstract Serializable getFirst() throws InterruptedException;

  /**
   * removes the first element in the queue
   *
   * @return the first element in the queue
   * @throws InterruptedException
   */
  protected abstract Serializable removeFirst() throws InterruptedException;

  /**
   * Adds an item at the end of the queue
   *
   * @param item object to add
   */
  protected abstract void add(Serializable item);

  /**
   * Adds an object at the beginning of the queue
   *
   * @param item object to add
   * @throws InterruptedException
   */
  protected abstract void addFirst(Serializable item) throws InterruptedException;

  /**
   * Adds all the items at the end of the queue
   *
   * @param items objects to add
   * @return true if it were able to add them all, false otherwise
   */
  protected abstract boolean doAddAll(Collection<? extends Serializable> items);

  /**
   * Removes all the items in the queue
   */
  protected abstract void doClear();

  /**
   * @return true if the queue is empty, false otherwise
   */
  protected abstract boolean isEmpty();
}
