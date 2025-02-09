/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.source.legacy;

import static org.mule.runtime.module.extension.internal.runtime.source.legacy.LegacyPollItemStatusUtils.from;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.core.internal.util.message.SdkResultAdapter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Adapts a {@link org.mule.sdk.api.runtime.source.PollContext} into a legacy {@link PollContext}
 *
 * @param <T> the generic type of the output value
 * @param <A> the generic type of the message attributes
 * @since 4.4.0
 */
public class LegacyPollContextAdapter<T, A> implements PollContext<T, A> {

  private final org.mule.sdk.api.runtime.source.PollContext delegate;

  public LegacyPollContextAdapter(org.mule.sdk.api.runtime.source.PollContext<T, A> delegate) {
    this.delegate = delegate;
  }

  @Override
  public PollItemStatus accept(Consumer<PollItem<T, A>> consumer) {
    return from(delegate.accept(new PollItemConsumerAdapter(consumer)));
  }

  @Override
  public Optional<Serializable> getWatermark() {
    return delegate.getWatermark();
  }

  @Override
  public boolean isSourceStopping() {
    return delegate.isSourceStopping();
  }

  @Override
  public void setWatermarkComparator(Comparator<? extends Serializable> comparator) {
    delegate.setWatermarkComparator(comparator);
  }

  @Override
  public void onConnectionException(ConnectionException e) {
    delegate.onConnectionException(e);
  }

  private static class PollItemConsumerAdapter<T, A>
      implements Consumer<org.mule.sdk.api.runtime.source.PollContext.PollItem<T, A>> {

    Consumer<PollItem<T, A>> delegate;

    PollItemConsumerAdapter(Consumer<PollItem<T, A>> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void accept(org.mule.sdk.api.runtime.source.PollContext.PollItem pollItem) {
      delegate.accept(new SdkToLegacyPollItemAdapter(pollItem));
    }

    private static class SdkToLegacyPollItemAdapter implements PollContext.PollItem {

      org.mule.sdk.api.runtime.source.PollContext.PollItem delegate;

      public SdkToLegacyPollItemAdapter(org.mule.sdk.api.runtime.source.PollContext.PollItem delegate) {
        this.delegate = delegate;
      }

      @Override
      public SourceCallbackContext getSourceCallbackContext() {
        return new LegacySourceCallbackContextAdapter(delegate.getSourceCallbackContext());
      }

      @Override
      public PollItem setResult(Result result) {
        delegate.setResult(SdkResultAdapter.from(result));
        return this;
      }

      @Override
      public PollItem setWatermark(Serializable watermark) {
        delegate.setWatermark(watermark);
        return this;
      }

      @Override
      public PollItem setId(String id) {
        delegate.setId(id);
        return this;
      }
    }
  }
}
