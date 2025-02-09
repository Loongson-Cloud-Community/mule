/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.tls.api;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import static java.util.Collections.emptyMap;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.AbstractTlsContextFactoryBuilderFactory;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.module.tls.internal.DefaultTlsContextFactory;

/**
 * Default implementation of {@link AbstractTlsContextFactoryBuilderFactory} which has a default TLS context. This is injected
 * into each new {@link TlsContextFactoryBuilder} so that a single instance is exposed.
 *
 * @since 4.0
 */
public final class DefaultTlsContextFactoryBuilderFactory extends AbstractTlsContextFactoryBuilderFactory {

  private TlsContextFactory defaultTlsContextFactory = new DefaultTlsContextFactory(emptyMap());

  public DefaultTlsContextFactoryBuilderFactory() {
    try {
      initialiseIfNeeded(defaultTlsContextFactory);
    } catch (InitialisationException e) {
      throw new MuleRuntimeException(createStaticMessage("Failed to initialise default TlsContextFactory"), e);
    }
  }

  @Override
  protected TlsContextFactoryBuilder create() {
    return new org.mule.runtime.module.tls.internal.DefaultTlsContextFactoryBuilder(defaultTlsContextFactory);
  }

}
