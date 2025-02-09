/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.context.notification;

import static java.util.Objects.requireNonNull;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.serverNotificationManagerNotEnabled;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.api.notification.Notification;
import org.mule.runtime.api.notification.NotificationListener;
import org.mule.runtime.api.notification.NotificationListenerRegistry;

import java.util.function.Predicate;

import javax.inject.Inject;

/**
 * Implementation of {@link NotificationListenerRegistry} registers listeners using a ServerNotificationHandler implementation.
 * 
 * @since 4.0
 */
public class DefaultNotificationListenerRegistry implements NotificationListenerRegistry {

  @Inject
  private MuleContext context;

  @Override
  public <N extends Notification> void registerListener(NotificationListener<N> listener) {
    requireNonNull(context.getNotificationManager(), serverNotificationManagerNotEnabled().getMessage());
    context.getNotificationManager().addListener(listener);
  }

  @Override
  public <N extends Notification> void registerListener(NotificationListener<N> listener, Predicate<N> selector) {
    requireNonNull(context.getNotificationManager(), serverNotificationManagerNotEnabled().getMessage());
    requireNonNull(selector);
    context.getNotificationManager().addListenerSubscription(listener, selector);
  }

  @Override
  public <N extends Notification> void unregisterListener(NotificationListener<N> listener) {
    if (context.getNotificationManager() != null) {
      context.getNotificationManager().removeListener(listener);
    }
  }

}
