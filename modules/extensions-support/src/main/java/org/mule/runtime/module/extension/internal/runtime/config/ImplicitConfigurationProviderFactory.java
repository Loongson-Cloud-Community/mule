/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.config;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

/**
 * Provides implicit configuration providers which are compliant with a {@link ConfigurationModel}. These are used when no
 * configuration has been specified. A best effort is made to automatically provide a default one but it may not be possible.
 *
 * @since 3.8.0
 */
public interface ImplicitConfigurationProviderFactory {

  /**
   * Creates an implicit configuration provider
   *
   * @param extensionModel             the {@link ExtensionModel} from which a {@link ConfigurationModel} is to be selected
   * @param implicitConfigurationModel the {@link ConfigurationModel} to be created.
   * @param muleEvent                  the current {@link CoreEvent}
   * @param reflectionCache            the {@link ReflectionCache} used to improve reflection lookups performance
   * @param expressionManager          the {@link ExpressionManager} used to create a session used to evaluate the attributes.
   *
   * @return a {@link ConfigurationProvider}
   * @throws IllegalStateException if it's not possible to create an implicit configuration automatically
   */
  ConfigurationProvider createImplicitConfigurationProvider(ExtensionModel extensionModel,
                                                            ConfigurationModel implicitConfigurationModel,
                                                            CoreEvent muleEvent,
                                                            ReflectionCache reflectionCache,
                                                            ExpressionManager expressionManager);
}
