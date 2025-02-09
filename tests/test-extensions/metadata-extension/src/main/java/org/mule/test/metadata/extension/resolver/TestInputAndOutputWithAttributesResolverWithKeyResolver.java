/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.metadata.extension.resolver;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;
import org.mule.runtime.api.metadata.resolving.AttributesTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;

import java.util.Set;

public class TestInputAndOutputWithAttributesResolverWithKeyResolver implements TypeKeysResolver,
    InputTypeResolver<String>, OutputTypeResolver<String>, AttributesTypeResolver<String> {

  public static final String TEST_INPUT_AND_OUTPUT_WITH_ATTRIBUTES_RESOLVER_WITH_KEY_RESOLVER =
      "TestInputAndOutputWithAttributesResolverWithKeyResolver";

  @Override
  public Set<MetadataKey> getKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    return TestMetadataResolverUtils.getKeys(context);
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, String key) throws MetadataResolvingException {
    return TestMetadataResolverUtils.getMetadata(key);
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, String key) throws MetadataResolvingException {
    return TestMetadataResolverUtils.getMetadata(key);
  }

  @Override
  public MetadataType getAttributesType(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    return TestMetadataResolverUtils.getMetadata(key);
  }

  @Override
  public String getCategoryName() {
    return "MetadataExtensionResolver";
  }

  @Override
  public String getResolverName() {
    return TEST_INPUT_AND_OUTPUT_WITH_ATTRIBUTES_RESOLVER_WITH_KEY_RESOLVER;
  }
}
