/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.connector;

import org.mule.runtime.api.exception.LocatedMuleException;
import org.mule.runtime.api.i18n.I18nMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Executor;

/**
 * When this exception is thrown it will trigger a retry (reconnection) policy to go into effect if one is configured.
 */
public class ConnectException extends LocatedMuleException {

  /** Serial version */
  private static final long serialVersionUID = -7802483584780922651L;

  /** Resource which has disconnected */
  private transient Connectable failed;

  public ConnectException(I18nMessage message, Connectable failed) {
    super(message, failed);
    // In the case of a MessageReceiver/MessageDispatcher, what we really want to reconnect is the Connector
    this.failed = failed;
  }

  public ConnectException(I18nMessage message, Throwable cause, Connectable failed) {
    super(message, cause, failed);
    // In the case of a MessageReceiver/MessageDispatcher, what we really want to reconnect is the Connector
    this.failed = failed;
  }

  public ConnectException(Throwable cause, Connectable failed) {
    super(cause, failed);
    // In the case of a MessageReceiver/MessageDispatcher, what we really want to reconnect is the Connector
    this.failed = failed;
  }

  public Connectable getFailed() {
    return failed;
  }

  private void writeObject(ObjectOutputStream out) throws Exception {
    out.defaultWriteObject();
    if (this.failed instanceof Serializable) {
      out.writeBoolean(true);
      out.writeObject(this.failed);
    } else {
      out.writeBoolean(false);
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    boolean failedWasSerialized = in.readBoolean();
    if (failedWasSerialized) {
      this.failed = (Connectable) in.readObject();
    }
  }

  public void handleReconnection(Executor retryExecutor) {
    // TODO See MULE-9307 - read reconnection behaviour for configs and sources
  }
}
