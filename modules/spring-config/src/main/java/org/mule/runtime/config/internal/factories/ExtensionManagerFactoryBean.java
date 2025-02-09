/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.factories;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.extension.ExtensionManager;

import javax.inject.Inject;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} which returns the {@link ExtensionManager} obtained through {@link MuleContext#getExtensionManager()}.
 * The purpose of that is to put the extensionManager in the context, even though it makes exist before it.
 *
 * @since 3.7.0
 */
public class ExtensionManagerFactoryBean implements FactoryBean<ExtensionManager> {

  private final MuleContext muleContext;

  @Inject
  public ExtensionManagerFactoryBean(MuleContext muleContext) {
    this.muleContext = muleContext;
  }

  @Override
  public ExtensionManager getObject() throws Exception {
    return muleContext.getExtensionManager();
  }

  @Override
  public Class<?> getObjectType() {
    return ExtensionManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
