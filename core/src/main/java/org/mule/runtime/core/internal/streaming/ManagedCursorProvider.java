/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.streaming;

import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for a {@link CursorProvider} decorator which makes sure that {@link Cursor cursors} opened by the decorated provider
 * are properly tracked through the {@link CursorManager}.
 *
 * @param <T> the generic type of the actual {@link Cursor} types that will be produced
 * @see CursorManager
 * @since 4.0
 */
public abstract class ManagedCursorProvider<T extends Cursor> extends CursorProviderDecorator<T>
    implements IdentifiableCursorProvider<T> {

  private final AtomicInteger openCursorsCount = new AtomicInteger(0);
  private final MutableStreamingStatistics statistics;
  private final CursorProviderJanitor janitor;
  private final int id;

  protected ManagedCursorProvider(IdentifiableCursorProvider<T> delegate, MutableStreamingStatistics statistics) {
    super(delegate);
    id = delegate.getId();
    this.janitor = new CursorProviderJanitor(delegate, openCursorsCount, statistics);
    this.statistics = statistics;
    if (statistics != null) {
      statistics.incrementOpenProviders();
    }
  }

  /**
   * Gets a cursor from the {@link #delegate} and keeps track of it.
   * <p>
   * The returned cursor will also be managed through the means of {@link #managedCursor(Cursor)}
   *
   * @return a new {@link Cursor}
   */
  @Override
  public final T openCursor() {
    T cursor = delegate.openCursor();
    openCursorsCount.incrementAndGet();

    if (statistics != null) {
      statistics.incrementOpenCursors();
    }

    return managedCursor(cursor);
  }

  /**
   * Returns a managed version of the {@code cursor}. How will that cursor be managed depends on each implementation. Although it
   * is possible that the same input {@code cursor} is returned, the assumption should be that a new instance will be returned.
   *
   * @param cursor the cursor to manage
   * @return a managed {@link Cursor}
   */
  protected abstract T managedCursor(T cursor);

  @Override
  public final void releaseResources() {
    janitor.releaseResources();
  }

  @Override
  public int getId() {
    return id;
  }

  public CursorProviderJanitor getJanitor() {
    return janitor;
  }

  @Override
  public void close() {
    janitor.close();
  }
}
