/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.testmodels.mule;

import org.mule.runtime.api.notification.NotificationDispatcher;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.core.privileged.transaction.AbstractSingleResourceTransaction;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.Transaction;

/**
 * A test transaction that does nothing on commit or rollback. The transaction does retain a status so that developers can
 * determine if the the transaction was rolled back or committed.
 */
public class TestTransaction extends AbstractSingleResourceTransaction {

  private AtomicBoolean committed = new AtomicBoolean(false);
  private AtomicBoolean rolledBack = new AtomicBoolean(false);

  private String testProperty;
  private boolean isXA;

  public TestTransaction(String applicationName, NotificationDispatcher notificationFirer) {
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

  public TestTransaction(String applicationName, NotificationDispatcher notificationFirer, boolean isXa) {
    super(applicationName, notificationFirer);
    this.isXA = isXa;
  }

  /**
   * Really begin the transaction. Note that resources are enlisted yet.
   *
   * @throws TransactionException
   *
   */
  protected void doBegin() throws TransactionException {
    // do nothing
  }

  /**
   * Commit the transaction on the underlying resource
   *
   * @throws TransactionException
   *
   */
  protected void doCommit() throws TransactionException {
    committed.set(true);
  }

  /**
   * Rollback the transaction on the underlying resource
   *
   * @throws TransactionException
   *
   */
  protected void doRollback() throws TransactionException {
    rolledBack.set(true);
  }

  public String getTestProperty() {
    return testProperty;
  }

  public void setTestProperty(String testProperty) {
    this.testProperty = testProperty;
  }

  @Override
  public boolean isXA() {
    return isXA;
  }


  public void setXA(boolean xa) {
    isXA = xa;
  }

  @Override
  public Transaction suspend() throws TransactionException {
    if (isXA) {
      return null;
    }
    return super.suspend();
  }

  @Override
  public void resume() throws TransactionException {
    if (isXA) {
      return;
    }
    super.suspend();
  }
}
