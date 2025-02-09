/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.metadata;

import static java.util.Collections.unmodifiableSet;
import static org.mule.runtime.module.extension.api.metadata.MultilevelMetadataKeyBuilder.newKey;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link TypeKeysResolver} implementation which resolves automatically {@link MetadataKey}s for boolean based MetadataKey Id
 * parameters.
 * </p>
 * This resolver will only return the possible values of a boolean: {@code true} and {@code false}
 *
 * @since 4.0
 * @see TypeKeysResolver
 */
public final class BooleanKeyResolver implements TypeKeysResolver {

  private final static Set<MetadataKey> keys = new HashSet<MetadataKey>() {

    {
      add(newKey("TRUE").build());
      add(newKey("FALSE").build());
    }
  };
  private final String categoryName;

  public BooleanKeyResolver(String categoryName) {
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
    return unmodifiableSet(keys);
  }
}
