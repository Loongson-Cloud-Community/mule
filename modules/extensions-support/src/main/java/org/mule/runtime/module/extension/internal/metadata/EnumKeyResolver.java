/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.metadata;

import static java.util.stream.Collectors.toSet;

import org.mule.metadata.api.annotation.EnumAnnotation;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeyBuilder;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;

import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link TypeKeysResolver} implementation which resolves automatically {@link MetadataKey}s for {@link Enum} based MetadataKey Id
 * parameters.
 *
 * @since 4.0
 * @see TypeKeysResolver
 */
public final class EnumKeyResolver implements TypeKeysResolver {

  private final Set<MetadataKey> keys;
  private final String categoryName;

  /**
   * @param anEnum       An {@link Enum} represented by a {@link EnumAnnotation} of a {@link MetadataType}
   * @param categoryName Category name of the current {@link TypeKeysResolver}
   */
  public EnumKeyResolver(EnumAnnotation anEnum, String categoryName) {
    keys = Stream.of(anEnum.getValues())
        .map(Object::toString)
        .map(MetadataKeyBuilder::newKey)
        .map(MetadataKeyBuilder::build)
        .collect(toSet());
    this.categoryName = categoryName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCategoryName() {
    return categoryName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<MetadataKey> getKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    return keys;
  }
}
