/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transaction.xa;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.core.api.transaction.xa.ResourceManagerException;

import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.slf4j.Logger;

/**
 * Base class for an XAResource implementation.
 *
 * @param <T> type of the {@link AbstractXaTransactionContext} created for each transaction
 */
public abstract class DefaultXASession<T extends AbstractXaTransactionContext> implements XAResource {

  private static final Logger LOGGER = getLogger(DefaultXASession.class);

  private Xid localXid;
  private final AbstractXAResourceManager<T> resourceManager;
  private T localContext;

  public DefaultXASession(AbstractXAResourceManager<T> resourceManager) {
    this.localContext = null;
    this.localXid = null;
    this.resourceManager = requireNonNull(resourceManager);
  }

  public XAResource getXAResource() {
    return this;
  }

  public Object getResourceManager() {
    return resourceManager;
  }

  //
  // XAResource implementation
  //

  @Override
  public boolean isSameRM(XAResource xares) throws XAException {
    return xares instanceof DefaultXASession && ((DefaultXASession) xares).getResourceManager().equals(resourceManager);
  }

  @Override
  public void start(Xid xid, int flags) throws XAException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder(128).append("Thread ").append(currentThread())
          .append(flags == TMNOFLAGS ? " starts" : flags == TMJOIN ? " joins" : " resumes")
          .append(" work on behalf of transaction branch ").append(xid).toString());
    }
    // A local transaction is already begun
    if (this.localContext != null) {
      throw new XAException(XAException.XAER_PROTO);
    }
    // This session has already been associated with an xid
    if (this.localXid != null) {
      throw new XAException(XAException.XAER_PROTO);
    }
    switch (flags) {
      // a new transaction
      case TMNOFLAGS:
      case TMJOIN:
      default:
        try {
          localContext = createTransactionContext(xid);
          resourceManager.beginTransaction(localContext);
        } catch (Exception e) {
          // TODO MULE-863: Is logging necessary?
          LOGGER.error("Could not create new transactional resource", e);
          throw (XAException) new XAException(e.getMessage()).initCause(e);
        }
        break;
      case TMRESUME:
        localContext = resourceManager.getSuspendedTransactionalResource(xid);
        if (localContext == null) {
          throw new XAException(XAException.XAER_NOTA);
        }
        // TODO: resume context
        resourceManager.removeSuspendedTransactionalResource(xid);
        break;
    }
    localXid = xid;
    resourceManager.addActiveTransactionalResource(localXid, localContext);
  }

  @Override
  public void end(Xid xid, int flags) throws XAException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder(128).append("Thread ").append(currentThread())
          .append(flags == TMSUSPEND ? " suspends" : flags == TMFAIL ? " fails" : " ends")
          .append(" work on behalf of transaction branch ").append(xid).toString());
    }
    // No transaction is already begun
    if (localContext == null) {
      throw new XAException(XAException.XAER_NOTA);
    }
    // This session has already been associated with an xid
    if (localXid == null || !localXid.equals(xid)) {
      throw new XAException(XAException.XAER_PROTO);
    }

    try {
      switch (flags) {
        case TMSUSPEND:
          // TODO: suspend context
          resourceManager.addSuspendedTransactionalResource(localXid, localContext);
          resourceManager.removeActiveTransactionalResource(localXid);
          break;
        case TMFAIL:
          resourceManager.setTransactionRollbackOnly(localContext);
          break;
        case TMSUCCESS: // no-op
        default: // no-op
          break;
      }
    } catch (ResourceManagerException e) {
      throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
    }
    localXid = null;
    localContext = null;
  }

  @Override
  public void commit(Xid xid, boolean onePhase) throws XAException {
    if (xid == null) {
      throw new XAException(XAException.XAER_PROTO);
    }
    T context = resourceManager.getActiveTransactionalResource(xid);
    if (context == null) {
      LOGGER.debug("Commit called without a transaction context");
      commitDanglingTransaction(xid, onePhase);
      return;
    }
    LOGGER.debug("Committing transaction branch {}", xid);
    if (context.status == Status.STATUS_MARKED_ROLLBACK) {
      throw new XAException(XAException.XA_RBROLLBACK);
    }

    try {
      if (context.status != Status.STATUS_PREPARED) {
        if (onePhase) {
          resourceManager.prepareTransaction(context);
        } else {
          throw new XAException(XAException.XAER_PROTO);
        }
      }
      resourceManager.commitTransaction(context);
      localContext = null;
    } catch (ResourceManagerException e) {
      throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
    }
    resourceManager.removeActiveTransactionalResource(xid);
    resourceManager.removeSuspendedTransactionalResource(xid);
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    if (xid == null) {
      throw new XAException(XAException.XAER_PROTO);
    }
    AbstractTransactionContext context = resourceManager.getActiveTransactionalResource(xid);
    if (context == null) {
      LOGGER.debug("Rollback called without a transaction context");
      rollbackDandlingTransaction(xid);
      return;
    }
    LOGGER.debug("Rolling back transaction branch {}", xid);
    try {
      resourceManager.rollbackTransaction(context);
      localContext = null;
    } catch (ResourceManagerException e) {
      throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
    }
    resourceManager.removeActiveTransactionalResource(xid);
    resourceManager.removeSuspendedTransactionalResource(xid);
  }

  @Override
  public int prepare(Xid xid) throws XAException {
    if (xid == null) {
      throw new XAException(XAException.XAER_PROTO);
    }

    T context = resourceManager.getTransactionalResource(xid);
    if (context == null) {
      throw new XAException(XAException.XAER_NOTA);
    }

    LOGGER.debug("Preparing transaction branch {}", xid);

    if (context.status == Status.STATUS_MARKED_ROLLBACK) {
      throw new XAException(XAException.XA_RBROLLBACK);
    }

    try {
      return resourceManager.prepareTransaction(context);
    } catch (ResourceManagerException e) {
      throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
    }
  }

  @Override
  public void forget(Xid xid) throws XAException {
    LOGGER.debug("Forgetting transaction branch {}", xid);
    AbstractTransactionContext context = resourceManager.getTransactionalResource(xid);
    if (context == null) {
      throw new XAException(XAException.XAER_NOTA);
    }
    resourceManager.removeActiveTransactionalResource(xid);
    resourceManager.removeSuspendedTransactionalResource(xid);
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return (int) (resourceManager.getDefaultTransactionTimeout() / 1000);
  }

  @Override
  public boolean setTransactionTimeout(int timeout) throws XAException {
    resourceManager.setDefaultTransactionTimeout(timeout * 1000);
    return false;
  }

  public T getTransactionContext() {
    return this.localContext;
  }

  /**
   * Commits a dangling transaction that can be caused by the failure of one of the XAResource involved in the transaction or a
   * crash of the transaction manager.
   *
   * @param xid      transaction identifier
   * @param onePhase if the commit should be done using only one phase commit
   * @throws XAException
   */
  protected abstract void commitDanglingTransaction(Xid xid, boolean onePhase) throws XAException;

  /**
   * Commits a dangling transaction that can be caused by the failure of one of the XAResource involved in the transaction or a
   * crash of the transaction manager.
   *
   * @param xid transaction identifier
   * @throws XAException
   */
  protected abstract void rollbackDandlingTransaction(Xid xid) throws XAException;

  /**
   * Creates a new transaction context with the given transaction identifier
   *
   * @param xid transaction identifier
   * @return the new transaction context
   */
  abstract protected T createTransactionContext(Xid xid);

}
