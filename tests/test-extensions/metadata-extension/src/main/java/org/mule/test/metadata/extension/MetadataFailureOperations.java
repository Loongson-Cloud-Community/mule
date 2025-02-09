/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.metadata.extension;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.test.metadata.extension.resolver.TestMetadataResolverMetadataResolvingFailure;
import org.mule.test.metadata.extension.resolver.TestMetadataResolverRuntimeExceptionFailure;

public class MetadataFailureOperations extends MetadataOperationsParent {

  // NamedTypeResolver throws MetadataResolvingException
  @OutputResolver(output = TestMetadataResolverMetadataResolvingFailure.class)
  @MediaType(value = ANY, strict = false)
  public Object failWithResolvingException(@Connection MetadataConnection connection,
                                           @MetadataKeyId(TestMetadataResolverMetadataResolvingFailure.class) String type,
                                           @org.mule.sdk.api.annotation.param.Content @TypeResolver(TestMetadataResolverMetadataResolvingFailure.class) Object content) {
    return null;
  }

  // Resolver for content and output type
  // With keysResolver and KeyParam
  @OutputResolver(output = TestMetadataResolverRuntimeExceptionFailure.class)
  @MediaType(value = ANY, strict = false)
  public Object failWithRuntimeException(@Connection MetadataConnection connection,
                                         @MetadataKeyId(TestMetadataResolverRuntimeExceptionFailure.class) String type,
                                         @Content @TypeResolver(TestMetadataResolverRuntimeExceptionFailure.class) Object content) {
    return null;
  }

}
