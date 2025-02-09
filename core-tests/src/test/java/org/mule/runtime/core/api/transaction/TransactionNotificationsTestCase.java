/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.transaction;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.mule.runtime.api.notification.TransactionNotification.TRANSACTION_BEGAN;
import static org.mule.runtime.api.notification.TransactionNotification.TRANSACTION_COMMITTED;
import static org.mule.runtime.api.notification.TransactionNotification.TRANSACTION_ROLLEDBACK;
import static org.mule.tck.util.MuleContextUtils.getNotificationDispatcher;

import org.mule.runtime.api.notification.IntegerAction;
import org.mule.runtime.api.notification.NotificationDispatcher;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.api.notification.TransactionNotification;
import org.mule.runtime.api.notification.TransactionNotificationListener;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.runtime.core.privileged.transaction.AbstractSingleResourceTransaction;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class TransactionNotificationsTestCase extends AbstractMuleContextTestCase {

  @Test
  public void testTransactionNotifications() throws Exception {
    final CountDownLatch latch = new CountDownLatch(3);

    // the code is simple and deceptive :) The trick is this dummy transaction is handled by
    // a global TransactionCoordination instance, which binds it to the current thread.
    Transaction transaction =
        new DummyTransaction("appName",
                             getNotificationDispatcher(muleContext));

    ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(NotificationListenerRegistry.class)
        .registerListener(new TransactionNotificationListener<TransactionNotification>() {

          @Override
          public boolean isBlocking() {
            return false;
          }

          @Override
          public void onNotification(TransactionNotification notification) {
            if (new IntegerAction(TRANSACTION_BEGAN).equals(notification.getAction())) {
              assertEquals("begin", notification.getActionName());
              latch.countDown();
            } else if (new IntegerAction(TRANSACTION_COMMITTED).equals(notification.getAction())) {
              assertEquals("commit", notification.getActionName());
              latch.countDown();
            } else if (new IntegerAction(TRANSACTION_ROLLEDBACK).equals(notification.getAction())) {
              assertEquals("rollback", notification.getActionName());
              latch.countDown();
            }
          }
        }, notification -> transaction.getId().equals(notification.getResourceIdentifier()));

    transaction.begin();
    transaction.commit();
    transaction.rollback();

    // Wait for the notification event to be fired as they are queued
    latch.await(2000, MILLISECONDS);

    assertEquals("There are still some notifications left unfired.", 0, latch.getCount());
  }

  private class DummyTransaction extends AbstractSingleResourceTransaction {

    private DummyTransaction(String applicationName,
                             NotificationDispatcher notificationFirer) {
      super(applicationName, notificationFirer);
    }

    @Override
    protected Class getResourceType() {
      return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Class getKeyType() {
      return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doBegin() {

    }

    @Override
    protected void doCommit() {

    }

    @Override
    protected void doRollback() {

    }
  }

}
