/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.runtime.ExpirationPolicy;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.runtime.module.extension.internal.runtime.resolver.ConnectionProviderValueResolver;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolver;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

/**
 * A factory which creates instances of {@link ConfigurationProvider}
 *
 * @since 4.0
 */
public interface ConfigurationProviderFactory {

  /**
   * Creates a new {@link ConfigurationProvider} which servers instances of a dynamic configuration
   *
   * @param name                       the provider's name
   * @param extensionModel             the {@link ExtensionModel} which owns the {@code configurationModel}
   * @param configurationModel         the {@link ConfigurationModel} that describes the configuration instances to be returned
   * @param resolverSet                a {@link ResolverSet} for the configuration's attributes
   * @param connectionProviderResolver a {@link ValueResolver} to obtain a {@link ConnectionProvider}
   * @param expirationPolicy           an {@link ExpirationPolicy} in case the configuration is dynamic
   * @param reflectionCache            the {@link ReflectionCache} used to improve reflection lookups performance
   * @param expressionManager          the {@link ExpressionManager} used to create a session used to evaluate the attributes.
   * @param muleContext                the {@link MuleContext} that will own the configuration instances
   *
   * @return a {@link ConfigurationProvider}
   * @throws Exception if anything goes wrong
   */
  ConfigurationProvider createDynamicConfigurationProvider(String name,
                                                           ExtensionModel extensionModel,
                                                           ConfigurationModel configurationModel,
                                                           ResolverSet resolverSet,
                                                           ConnectionProviderValueResolver connectionProviderResolver,
                                                           ExpirationPolicy expirationPolicy,
                                                           ReflectionCache reflectionCache,
                                                           ExpressionManager expressionManager,
                                                           MuleContext muleContext)
      throws Exception;


  /**
   * Creates a new {@link ConfigurationProvider} which servers a static configuration instance
   *
   * @param name                       the provider's name
   * @param extensionModel             the {@link ExtensionModel} which owns the {@code configurationModel}
   * @param configurationModel         the {@link ConfigurationModel} that describes the configuration instances to be returned
   * @param resolverSet                a {@link ResolverSet} for the configuration's attributes
   * @param connectionProviderResolver A {@link ValueResolver} to obtain a {@link ConnectionProvider}
   * @param muleContext                the {@link MuleContext} that will own the configuration instances
   * @return a {@link ConfigurationProvider}
   * @throws Exception if anything goes wrong
   */
  ConfigurationProvider createStaticConfigurationProvider(String name,
                                                          ExtensionModel extensionModel,
                                                          ConfigurationModel configurationModel,
                                                          ResolverSet resolverSet,
                                                          ConnectionProviderValueResolver connectionProviderResolver,
                                                          ReflectionCache reflectionCache,
                                                          ExpressionManager expressionManager,
                                                          MuleContext muleContext)
      throws Exception;
}
