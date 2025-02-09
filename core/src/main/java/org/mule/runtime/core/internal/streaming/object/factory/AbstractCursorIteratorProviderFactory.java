/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.streaming.object.factory;

import static java.lang.Boolean.getBoolean;
import static org.mule.runtime.api.util.MuleSystemProperties.TRACK_CURSOR_PROVIDER_CLOSE_PROPERTY;
import static org.mule.runtime.core.privileged.util.EventUtils.getRoot;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.EventContext;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.object.CursorIterator;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.streaming.StreamingManager;
import org.mule.runtime.core.api.streaming.iterator.StreamingIterator;
import org.mule.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import org.mule.runtime.core.internal.streaming.CursorManager;

import java.util.Iterator;

/**
 * Base implementation of {@link CursorIteratorProviderFactory} which contains all the base behaviour and template methods.
 * <p>
 * It interacts with the {@link CursorManager} in order to track all allocated resources and make sure they're properly disposed
 * of once they're no longer necessary.
 *
 * @since 4.0
 */
public abstract class AbstractCursorIteratorProviderFactory implements CursorIteratorProviderFactory {

  protected final StreamingManager streamingManager;
  protected final static boolean trackCursorProviderClose = getBoolean(TRACK_CURSOR_PROVIDER_CLOSE_PROPERTY);

  public AbstractCursorIteratorProviderFactory(StreamingManager streamingManager) {
    this.streamingManager = streamingManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Object of(EventContext eventContext, Iterator iterator, ComponentLocation originatingLocation) {
    if (iterator instanceof CursorIterator) {
      return streamingManager.manage(((CursorIterator) iterator).getProvider(), eventContext);
    }

    Object value = resolve(iterator, eventContext, originatingLocation);
    if (value instanceof CursorProvider) {
      value = streamingManager.manage((CursorProvider) value, eventContext);
    }

    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Object of(EventContext eventContext, Iterator iterator) {
    return of(eventContext, iterator, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Object of(CoreEvent event, Iterator value, ComponentLocation originatingLocation) {
    return of(getRoot(event.getContext()), value, originatingLocation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Object of(CoreEvent event, Iterator value) {
    return of(getRoot(event.getContext()), value, null);
  }

  /**
   * Implementations should use this method to actually create the output value
   *
   * @param iterator            the streaming iterator
   * @param eventContext        the root context of the event on which streaming is happening
   * @param originatingLocation the {@link ComponentLocation} where the cursor was created
   */
  protected abstract Object resolve(Iterator iterator, EventContext eventContext, ComponentLocation originatingLocation);

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if the {@code value} is a {@link StreamingIterator}
   */
  @Override
  public boolean accepts(Object value) {
    return value instanceof Iterator;
  }
}
