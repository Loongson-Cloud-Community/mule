/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transaction.xa;

import org.mule.runtime.core.api.transaction.xa.ResourceManagerException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Status;
import javax.transaction.xa.Xid;

public abstract class AbstractXAResourceManager<T extends AbstractXaTransactionContext> extends AbstractResourceManager {

  private Map<Xid, T> suspendedContexts = new ConcurrentHashMap<>();
  private Map<Xid, T> activeContexts = new ConcurrentHashMap<>();

  public AbstractXAResourceManager() {
    super();
  }

  public int prepareTransaction(T context) throws ResourceManagerException {
    assureReady();
    synchronized (context) {
      if (logger.isDebugEnabled()) {
        logger.debug("Preparing transaction " + context);
      }
      context.status = Status.STATUS_PREPARING;
      int status = doPrepare(context);
      context.status = Status.STATUS_PREPARED;
      if (logger.isDebugEnabled()) {
        logger.debug("Prepared transaction " + context);
      }
      return status;
    }
  }

  protected abstract int doPrepare(T context) throws ResourceManagerException;

  T getTransactionalResource(Xid xid) {
    T context = getActiveTransactionalResource(xid);
    if (context != null) {
      return context;
    } else {
      return getSuspendedTransactionalResource(xid);
    }
  }

  T getActiveTransactionalResource(Xid xid) {
    return activeContexts.get(xid);
  }

  T getSuspendedTransactionalResource(Xid xid) {
    return suspendedContexts.get(xid);
  }

  void addActiveTransactionalResource(Xid xid, T context) {
    activeContexts.put(xid, context);
  }

  void addSuspendedTransactionalResource(Xid xid, T context) {
    suspendedContexts.put(xid, context);
  }

  void removeActiveTransactionalResource(Xid xid) {
    activeContexts.remove(xid);
  }

  void removeSuspendedTransactionalResource(Xid xid) {
    suspendedContexts.remove(xid);
  }
}
