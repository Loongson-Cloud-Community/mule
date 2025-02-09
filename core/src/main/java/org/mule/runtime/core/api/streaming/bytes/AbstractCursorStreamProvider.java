/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.streaming.bytes;

import static java.util.Optional.ofNullable;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.internal.streaming.CursorProviderAlreadyClosedException;
import org.mule.runtime.core.internal.streaming.ClosingCursorException;

/**
 * Base class for {@link CursorStreamProvider} implementations.
 *
 * @since 4.0
 */
@NoExtend
public abstract class AbstractCursorStreamProvider extends AbstractComponent implements CursorStreamProvider {

  protected final InputStream wrappedStream;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  private final ComponentLocation originatingLocation;

  private ClosingCursorException closerResponsible;

  private final boolean trackCursorProviderClose;

  /**
   * Creates a new instance
   *
   * @param wrappedStream            the original stream to be decorated
   * @param originatingLocation      indicates where the provider was created
   * @param trackCursorProviderClose if the provider should save the stack trace from where it was closed
   */
  public AbstractCursorStreamProvider(InputStream wrappedStream, ComponentLocation originatingLocation,
                                      boolean trackCursorProviderClose) {
    this.wrappedStream = wrappedStream;
    this.originatingLocation = originatingLocation;
    this.trackCursorProviderClose = trackCursorProviderClose;
  }

  /**
   * Creates a new instance
   *
   * @param wrappedStream the original stream to be decorated
   *
   * @deprecated Please use {@link #AbstractCursorStreamProvider(InputStream, ComponentLocation, boolean)} instead.
   */
  @Deprecated
  public AbstractCursorStreamProvider(InputStream wrappedStream) {
    this(wrappedStream, null, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final CursorStream openCursor() {
    if (closed.get()) {
      throw new CursorProviderAlreadyClosedException("Cannot open a new cursor on a closed stream",
                                                     getOriginatingLocation(),
                                                     ofNullable(closerResponsible));
    }
    return doOpenCursor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    closed.set(true);
    if (trackCursorProviderClose) {
      closerResponsible = new ClosingCursorException("Responsible for closing the stream.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isClosed() {
    return closed.get();
  }

  protected abstract CursorStream doOpenCursor();

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ComponentLocation> getOriginatingLocation() {
    return ofNullable(originatingLocation);
  }
}
