/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.util;

import static org.mule.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.meta.model.EnrichableModel;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.module.extension.internal.loader.ParameterGroupDescriptor;
import org.mule.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import org.mule.runtime.module.extension.internal.runtime.objectbuilder.ParameterGroupObjectBuilder;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSetResult;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

/**
 * An implementation of {@link ValueSetter} for parameter groups. Parameter groups are a set of parameters defined inside a Pojo
 * in order to reference them as a group and avoid code repetition. The parameter groups are defined by applying the
 * {@link org.mule.runtime.extension.api.annotation.param.ParameterGroup} annotation to a field.
 *
 * @since 3.7.0
 */
public final class GroupValueSetter implements ValueSetter {

  /**
   * Returns a {@link List} containing one {@link ValueSetter} instance per each {@link ParameterGroupDescriptor} defined in the
   * {@link ParameterGroupModelProperty} extracted from the given {@code model}. If {@code model} does not contain such model
   * property then an empty {@link List} is returned
   *
   * @param model           a {@link EnrichableModel} instance presumed to have the {@link ParameterGroupModelProperty}
   * @param reflectionCache the cache for expensive reflection lookups
   * @return a {@link List} with {@link ValueSetter} instances. May be empty but will never be {@code null}
   */
  public static List<ValueSetter> settersFor(ParameterizedModel model,
                                             Supplier<ReflectionCache> reflectionCache,
                                             Supplier<ExpressionManager> expressionManager) {
    ImmutableList.Builder<ValueSetter> setters = ImmutableList.builder();
    model.getParameterGroupModels().stream()
        .filter(group -> !group.isShowInDsl())
        .filter(group -> !group.getName().equals(DEFAULT_GROUP_NAME))
        .forEach(group -> group.getModelProperty(ParameterGroupModelProperty.class).ifPresent(property -> {
          if (property.getDescriptor().getContainer() instanceof Field) {
            setters.add(new GroupValueSetter(property.getDescriptor(), reflectionCache, expressionManager));
          }
        }));

    return setters.build();
  }

  private final ParameterGroupDescriptor groupDescriptor;
  private final Field container;
  private final Supplier<ReflectionCache> reflectionCache;
  private final Supplier<ExpressionManager> expressionManager;

  /**
   * Creates a new instance that can set values defined in the given {@code group}
   *
   * @param groupDescriptor   a {@link ParameterGroupDescriptor}
   * @param reflectionCache   the cache for expensive reflection lookups
   * @param expressionManager the {@link ExpressionManager} used to create a session used to evaluate the attributes.
   */
  public GroupValueSetter(ParameterGroupDescriptor groupDescriptor,
                          Supplier<ReflectionCache> reflectionCache,
                          Supplier<ExpressionManager> expressionManager) {
    this.groupDescriptor = groupDescriptor;
    checkArgument(groupDescriptor.getContainer() instanceof Field, "Only field contained parameter groups are allowed");
    container = (Field) groupDescriptor.getContainer();
    this.reflectionCache = reflectionCache;
    this.expressionManager = expressionManager;
  }

  @Override
  public void set(Object target, ResolverSetResult result) throws MuleException {
    container.setAccessible(true);
    ParameterGroupObjectBuilder<?> parameterGroupObjectBuilder = new ParameterGroupObjectBuilder<>(groupDescriptor,
                                                                                                   reflectionCache.get(),
                                                                                                   expressionManager.get());
    Object value = parameterGroupObjectBuilder.build(result);
    try {
      container.set(target, value);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
    }
  }
}
