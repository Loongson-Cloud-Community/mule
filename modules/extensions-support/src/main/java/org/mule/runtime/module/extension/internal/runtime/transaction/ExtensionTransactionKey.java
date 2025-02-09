/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.transaction;

import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.transaction.Transaction;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.runtime.module.extension.internal.runtime.operation.ExecutionContextConfigurationDecorator;

/**
 * The key used to bind a {@link ExtensionTransactionalResource} into a {@link Transaction}. Although logically speaking it is the
 * extension's {@link ConfigurationInstance} which should act as key, this class allows to decouple from its concrete type and
 * while not depending on its equals and hashCode implementations
 *
 * @since 4.0
 */
public class ExtensionTransactionKey {

  private final Reference<ConfigurationInstance> configReference;

  public ExtensionTransactionKey(ConfigurationInstance config) {
    configReference = new Reference<>(config instanceof ExecutionContextConfigurationDecorator
        ? ((ExecutionContextConfigurationDecorator) config).getDecorated()
        : config);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExtensionTransactionKey && configReference.equals(((ExtensionTransactionKey) obj).configReference);
  }

  @Override
  public int hashCode() {
    return configReference.hashCode();
  }
}
