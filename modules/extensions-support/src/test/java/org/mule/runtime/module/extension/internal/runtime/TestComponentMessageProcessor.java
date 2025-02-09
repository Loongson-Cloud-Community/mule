/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime;

import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.api.extension.ExtensionManager;
import org.mule.runtime.core.api.retry.policy.RetryPolicyTemplate;
import org.mule.runtime.core.api.streaming.CursorProviderFactory;
import org.mule.runtime.core.internal.policy.PolicyManager;
import org.mule.runtime.core.privileged.processor.chain.MessageProcessorChain;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.runtime.module.extension.internal.runtime.operation.ComponentMessageProcessor;
import org.mule.runtime.module.extension.internal.runtime.operation.ResultTransformer;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

public abstract class TestComponentMessageProcessor extends ComponentMessageProcessor<ComponentModel> {

  public TestComponentMessageProcessor(ExtensionModel extensionModel, ComponentModel componentModel,
                                       ConfigurationProvider configurationProvider, String target, String targetValue,
                                       ResolverSet resolverSet, CursorProviderFactory cursorProviderFactory,
                                       RetryPolicyTemplate retryPolicyTemplate, MessageProcessorChain nestedChain,
                                       ClassLoader classLoader, ExtensionManager extensionManager, PolicyManager policyManager,
                                       ReflectionCache reflectionCache, ResultTransformer resultTransformer,
                                       long terminationTimeout) {
    super(extensionModel, componentModel,
          configurationProvider != null ? new StaticValueResolver<>(configurationProvider) : null, target,
          targetValue,
          resolverSet, cursorProviderFactory,
          retryPolicyTemplate, nestedChain, classLoader, extensionManager, policyManager, reflectionCache, resultTransformer,
          terminationTimeout);
  }
}
