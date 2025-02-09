/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.module.extension.internal.runtime.config.DefaultImplicitConnectionProviderFactory;
import org.mule.runtime.module.extension.internal.runtime.config.ImplicitConnectionProviderFactory;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Optional;

/**
 * Uses a {@link ImplicitConnectionProviderFactory} to create an implicit {@link ConnectionProvider}.
 * <p>
 * This is a static {@link ValueResolver}. The {@link ConnectionProvider} is created the first time the
 * {@link #resolve(ValueResolvingContext)} method is invoked on {@code this} instance. Subsequent invocations will return the same
 * instance.
 * <p>
 * This class is thread-safe
 *
 * @since 4.0
 */
public final class ImplicitConnectionProviderValueResolver<C> implements ConnectionProviderValueResolver<C> {

  private final ImplicitConnectionProviderFactory implicitConnectionProviderFactory;
  private final String configName;

  public ImplicitConnectionProviderValueResolver(String name,
                                                 ExtensionModel extensionModel,
                                                 ConfigurationModel configurationModel,
                                                 ReflectionCache reflectionCache,
                                                 ExpressionManager expressionManager,
                                                 MuleContext muleContext) {
    configName = name;
    implicitConnectionProviderFactory = new DefaultImplicitConnectionProviderFactory(extensionModel,
                                                                                     configurationModel,
                                                                                     reflectionCache,
                                                                                     expressionManager,
                                                                                     muleContext);
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> resolve(ValueResolvingContext context) {
    return implicitConnectionProviderFactory.createImplicitConnectionProvider(configName, context.getEvent());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return implicitConnectionProviderFactory.isDynamic();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ResolverSet> getResolverSet() {
    return implicitConnectionProviderFactory.getResolverSet();
  }
}
