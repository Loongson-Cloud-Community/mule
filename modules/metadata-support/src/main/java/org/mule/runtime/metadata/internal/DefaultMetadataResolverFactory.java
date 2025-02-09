/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.metadata.internal;

import static java.util.Collections.unmodifiableCollection;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import org.mule.runtime.api.metadata.resolving.AttributesTypeResolver;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.api.metadata.resolving.QueryEntityResolver;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;
import org.mule.runtime.extension.api.metadata.MetadataResolverFactory;
import org.mule.runtime.extension.api.metadata.NullMetadataResolver;
import org.mule.runtime.extension.api.metadata.NullQueryMetadataResolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Default implementation of the {@link MetadataResolverFactory}, it provides initialized instances of {@link TypeKeysResolver},
 * {@link TypeKeysResolver} and {@link OutputTypeResolver} of the classes passed in the constructor.
 *
 * @since 4.0
 */
public final class DefaultMetadataResolverFactory implements MetadataResolverFactory {

  private final OutputTypeResolver outputTypeResolver;
  private final AttributesTypeResolver attributesTypeResolver;
  private final Map<String, InputTypeResolver> inputResolvers = new HashMap<>();
  private final TypeKeysResolver keysResolver;

  public DefaultMetadataResolverFactory(Supplier<? extends TypeKeysResolver> keyResolver,
                                        Map<String, Supplier<? extends InputTypeResolver>> typeResolvers,
                                        Supplier<? extends OutputTypeResolver> outputResolver,
                                        Supplier<? extends AttributesTypeResolver> attributesResolver) {

    checkArgument(keyResolver != null, "MetadataKeyResolver type cannot be null");
    checkArgument(typeResolvers != null, "InputTypeResolvers cannot be null");
    checkArgument(outputResolver != null, "OutputTypeResolver type cannot be null");
    checkArgument(attributesResolver != null, "AttributesTypeResolver type cannot be null");

    typeResolvers.forEach((k, v) -> inputResolvers.put(k, v.get()));
    keysResolver = keyResolver.get();
    outputTypeResolver = outputResolver.get();
    attributesTypeResolver = attributesResolver.get();

    checkArgument(keysResolver != null, "MetadataKeyResolver type cannot be null");
    inputResolvers.values().forEach(resolver -> checkArgument(resolver != null, "Input Type Resolver cannot be null"));
    checkArgument(outputTypeResolver != null, "OutputTypeResolver type cannot be null");
    checkArgument(attributesTypeResolver != null, "AttributesTypeResolver type cannot be null");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeKeysResolver getKeyResolver() {
    return keysResolver;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> InputTypeResolver<T> getInputResolver(String parameterName) {
    return inputResolvers.getOrDefault(parameterName, new NullMetadataResolver());
  }

  /**
   * {@inheritDoc}
   */
  public Collection<InputTypeResolver> getInputResolvers() {
    return unmodifiableCollection(inputResolvers.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> OutputTypeResolver<T> getOutputResolver() {
    return outputTypeResolver;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> AttributesTypeResolver<T> getOutputAttributesResolver() {
    return attributesTypeResolver;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public QueryEntityResolver getQueryEntityResolver() {
    return new NullQueryMetadataResolver();
  }
}
