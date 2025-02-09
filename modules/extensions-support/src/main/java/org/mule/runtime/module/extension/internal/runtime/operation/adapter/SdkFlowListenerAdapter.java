/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.operation.adapter;

import org.mule.runtime.api.message.Message;
import org.mule.sdk.api.runtime.operation.FlowListener;

import java.util.function.Consumer;

/**
 * Adapts a legacy {@link org.mule.runtime.extension.api.runtime.operation.FlowListener} into a {@link FlowListener}
 *
 * @since 4.5.0
 */
public class SdkFlowListenerAdapter implements FlowListener {

  private final org.mule.runtime.extension.api.runtime.operation.FlowListener delegate;

  public SdkFlowListenerAdapter(org.mule.runtime.extension.api.runtime.operation.FlowListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onSuccess(Consumer<Message> handler) {
    delegate.onSuccess(handler);
  }

  @Override
  public void onError(Consumer<Exception> handler) {
    delegate.onError(handler);
  }

  @Override
  public void onComplete(Runnable handler) {
    delegate.onComplete(handler);
  }
}
