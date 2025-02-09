/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import static java.lang.String.format;
import static org.mule.runtime.extension.api.util.ExtensionModelUtils.getFirstImplicit;
import static org.mule.runtime.module.extension.internal.loader.utils.ImplicitObjectUtils.buildImplicitResolverSet;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.getAllConnectionProviders;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.connection.ConnectionProviderModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolvingContext;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Optional;

import javax.inject.Provider;

/**
 * Default implementation of {@link ImplicitConnectionProviderFactory}
 *
 * @since 4.0
 */
public final class DefaultImplicitConnectionProviderFactory<T> implements ImplicitConnectionProviderFactory {

  private final ExtensionModel extensionModel;
  private final ExpressionManager expressionManager;
  private final MuleContext muleContext;
  private Provider<ResolverSet> resolverSetProvider;
  private ConnectionProviderModel connectionProviderModel = null;
  private ResolverSet resolverSet = null;

  public DefaultImplicitConnectionProviderFactory(ExtensionModel extensionModel, ConfigurationModel configurationModel,
                                                  ReflectionCache reflectionCache,
                                                  ExpressionManager expressionManager,
                                                  MuleContext muleContext) {
    this.extensionModel = extensionModel;
    this.muleContext = muleContext;
    this.expressionManager = expressionManager;

    resolverSetProvider = () -> {
      synchronized (this) {
        if (resolverSet == null) {
          connectionProviderModel = getFirstImplicit(getAllConnectionProviders(extensionModel, configurationModel));

          if (connectionProviderModel == null) {
            throw new IllegalStateException(format(
                                                   "Configuration '%s' of extension '%s' does not define a connection provider and none can be created automatically. Please define one.",
                                                   configurationModel.getName(), extensionModel.getName()));
          }
          resolverSet = buildImplicitResolverSet(connectionProviderModel, reflectionCache, expressionManager, muleContext);
          resolverSetProvider = () -> resolverSet;
        }
        return resolverSet;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Pair<ConnectionProvider<T>, ResolverSetResult> createImplicitConnectionProvider(String configName, CoreEvent event) {
    ResolverSet resolverSet = resolverSetProvider.get();
    ConnectionProviderObjectBuilder<T> builder = new DefaultConnectionProviderObjectBuilder<>(connectionProviderModel,
                                                                                              resolverSet,
                                                                                              extensionModel,
                                                                                              expressionManager,
                                                                                              muleContext);
    builder.setOwnerConfigName(configName);
    try (ValueResolvingContext ctx = ValueResolvingContext.builder(event, expressionManager).build()) {
      return builder.build(ctx);
    } catch (MuleException e) {
      throw new MuleRuntimeException(e);
    }
  }

  /**
   * Checking if an implicit connection provider is dynamic implies that there is a suitable implicit connection provider for the
   * extension. If that implicit provider couldn't be found, an {@link IllegalStateException} will be thrown.
   */
  @Override
  public boolean isDynamic() {
    return resolverSetProvider.get().isDynamic();
  }

  @Override
  public Optional<ResolverSet> getResolverSet() {
    return Optional.of(resolverSetProvider.get());
  }
}
