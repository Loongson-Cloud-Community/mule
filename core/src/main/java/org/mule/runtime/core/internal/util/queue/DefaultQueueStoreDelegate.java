/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.queue;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The default QueueStoreDelegate. This uses a LinkedList to store the members of the queue.
 */
public class DefaultQueueStoreDelegate extends AbstractQueueStoreDelegate {

  private final LinkedList<Serializable> list;

  public DefaultQueueStoreDelegate(int capacity) {
    super(capacity);
    list = new LinkedList<>();
  }

  @Override
  protected void add(Serializable o) {
    list.addLast(o);
  }

  @Override
  protected Serializable removeFirst() {
    return list.removeFirst();
  }

  @Override
  protected boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  protected Serializable getFirst() {
    return list.getFirst();
  }

  @Override
  protected void addFirst(Serializable item) {
    list.addFirst(item);
  }

  @Override
  protected void doClear() {
    list.clear();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  protected boolean doAddAll(Collection<? extends Serializable> items) {
    return list.addAll(items);
  }

  @Override
  public void dispose() {
    doClear();
  }
}
