/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.operation;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.api.util.collection.SmallMap.copy;
import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static org.mule.runtime.internal.dsl.DslConstants.CONFIG_ATTRIBUTE_NAME;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.getClassLoader;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.extension.ExtensionManager;
import org.mule.runtime.core.api.retry.policy.RetryPolicyTemplate;
import org.mule.runtime.core.api.streaming.CursorProviderFactory;
import org.mule.runtime.core.internal.policy.PolicyManager;
import org.mule.runtime.core.privileged.processor.chain.MessageProcessorChain;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.runtime.module.extension.internal.runtime.ExtensionComponent;
import org.mule.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import org.mule.runtime.module.extension.internal.runtime.resolver.ParametersResolver;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolver;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;
import org.mule.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.Map;

/**
 * Base class for creating MessageProcessor instances of a given {@link ComponentModel}
 *
 * @since 4.0
 */
public abstract class ComponentMessageProcessorBuilder<M extends ComponentModel, P extends ExtensionComponent> {

  protected final ExtensionModel extensionModel;
  protected final M operationModel;
  protected final PolicyManager policyManager;
  protected final ReflectionCache reflectionCache;
  protected final MuleContext muleContext;
  protected final ExpressionManager expressionManager;
  protected final ComponentTracerFactory<CoreEvent> componentTracerFactory;
  protected Registry registry;
  protected final ExtensionConnectionSupplier extensionConnectionSupplier;
  protected ConfigurationProvider configurationProvider;
  protected long terminationTimeout;

  protected Map<String, ?> parameters;
  protected String target;
  protected String targetValue;
  protected CursorProviderFactory cursorProviderFactory;
  protected RetryPolicyTemplate retryPolicyTemplate;
  protected MessageProcessorChain nestedChain;
  protected ClassLoader classLoader;

  public ComponentMessageProcessorBuilder(ExtensionModel extensionModel,
                                          M operationModel,
                                          PolicyManager policyManager,
                                          ReflectionCache reflectionCache,
                                          ExpressionManager expressionManager,
                                          MuleContext muleContext,
                                          Registry registry) {
    checkArgument(extensionModel != null, "ExtensionModel cannot be null");
    checkArgument(operationModel != null, "OperationModel cannot be null");
    checkArgument(policyManager != null, "PolicyManager cannot be null");
    checkArgument(muleContext != null, "muleContext cannot be null");

    this.muleContext = muleContext;
    this.extensionModel = extensionModel;
    this.operationModel = operationModel;
    this.policyManager = policyManager;
    this.reflectionCache = reflectionCache;
    this.registry = registry;
    this.extensionConnectionSupplier = registry.lookupByType(ExtensionConnectionSupplier.class).get();
    this.componentTracerFactory = registry.lookupByType(ComponentTracerFactory.class).get();
    this.expressionManager = expressionManager;
    this.terminationTimeout = muleContext.getConfiguration().getShutdownTimeout();
  }

  public P build() {
    return withContextClassLoader(getClassLoader(extensionModel), () -> {
      try {
        final ExtensionManager extensionManager = muleContext.getExtensionManager();
        final ResolverSet operationArguments = getArgumentsResolverSet();

        P processor = createMessageProcessor(extensionManager, operationArguments);
        // TODO: MULE-5002 this should not be necessary but lifecycle issues when injecting message processors automatically
        muleContext.getInjector().inject(processor);
        return processor;
      } catch (Exception e) {
        throw new MuleRuntimeException(e);
      }
    });
  }

  protected abstract P createMessageProcessor(ExtensionManager extensionManager, ResolverSet operationArguments);

  protected ResolverSet getArgumentsResolverSet() throws ConfigurationException {
    final ResolverSet parametersResolverSet =
        ParametersResolver
            .fromValues(parameters, muleContext, reflectionCache, expressionManager, operationModel.getName())
            .getParametersAsResolverSet(operationModel, muleContext);

    final ResolverSet childsResolverSet =
        ParametersResolver
            .fromValues(parameters, muleContext, reflectionCache, expressionManager, operationModel.getName())
            .getNestedComponentsAsResolverSet(operationModel);

    return parametersResolverSet.merge(childsResolverSet);
  }

  public ComponentMessageProcessorBuilder<M, P> setConfigurationProvider(ConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setParameters(Map<String, ?> parameters) {
    this.parameters = copy(parameters);
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setTarget(String target) {
    this.target = target;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setTargetValue(String targetValue) {
    this.targetValue = targetValue;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setCursorProviderFactory(CursorProviderFactory cursorProviderFactory) {
    this.cursorProviderFactory = cursorProviderFactory;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setRetryPolicyTemplate(RetryPolicyTemplate retryPolicyTemplate) {
    this.retryPolicyTemplate = retryPolicyTemplate;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setNestedChain(MessageProcessorChain nestedChain) {
    this.nestedChain = nestedChain;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setTerminationTimeout(long terminationTimeout) {
    this.terminationTimeout = terminationTimeout;
    return this;
  }

  protected ValueResolver<ConfigurationProvider> getConfigurationProviderResolver() {
    // Uses the configurationProvider given to the builder if any, otherwise evaluates the parameters.
    return configurationProvider != null ? new StaticValueResolver<>(configurationProvider)
        : getConfigurationProviderResolver(parameters.get(CONFIG_ATTRIBUTE_NAME));
  }

  private ValueResolver<ConfigurationProvider> getConfigurationProviderResolver(Object configRefParameter) {
    if (configRefParameter instanceof ValueResolver) {
      return (ValueResolver<ConfigurationProvider>) configRefParameter;
    }

    if (configRefParameter instanceof ConfigurationProvider) {
      return new StaticValueResolver<>((ConfigurationProvider) configRefParameter);
    }

    return null;
  }
}
