/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.metadata.extension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static org.mule.test.metadata.extension.MetadataConnection.CAR;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Query;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.tck.message.StringAttributes;
import org.mule.test.metadata.extension.model.animals.Animal;
import org.mule.test.metadata.extension.model.animals.AnimalClade;
import org.mule.test.metadata.extension.model.animals.AnimalShelter;
import org.mule.test.metadata.extension.model.animals.Bear;
import org.mule.test.metadata.extension.model.attribute.AbstractOutputAttributes;
import org.mule.test.metadata.extension.model.shapes.Rectangle;
import org.mule.test.metadata.extension.model.shapes.Shape;
import org.mule.test.metadata.extension.model.shops.Dessert;
import org.mule.test.metadata.extension.model.shops.Order;
import org.mule.test.metadata.extension.query.MetadataExtensionEntityResolver;
import org.mule.test.metadata.extension.query.MetadataExtensionQueryTranslator;
import org.mule.test.metadata.extension.query.NativeQueryOutputResolver;
import org.mule.test.metadata.extension.resolver.AnyJsonTypeStaticResolver;
import org.mule.test.metadata.extension.resolver.SdkTestInputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.SdkTestOutputAnyTypeResolver;
import org.mule.test.metadata.extension.resolver.SdkTestOutputAttributesResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestAttributesResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestBooleanMetadataResolver;
import org.mule.test.metadata.extension.resolver.TestEnumMetadataResolver;
import org.mule.test.metadata.extension.resolver.TestInputAndOutputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam;
import org.mule.test.metadata.extension.resolver.TestInputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestInputResolverWithoutKeyResolver;
import org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver;
import org.mule.test.metadata.extension.resolver.TestOutputAnyTypeResolver;
import org.mule.test.metadata.extension.resolver.TestOutputAttributesResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestOutputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestOutputResolverWithKeyResolverUsingConfig;
import org.mule.test.metadata.extension.resolver.TestOutputResolverWithoutKeyResolver;
import org.mule.test.metadata.extension.resolver.TestPartialMultiLevelKeyResolver;
import org.mule.test.metadata.extension.resolver.TestResolverWithCache;
import org.mule.test.metadata.extension.resolver.TestThreadContextClassLoaderResolver;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetadataOperations {

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentMetadataWithKeyId(@Config Object object, @Connection MetadataConnection connection,
                                         @MetadataKeyId(TestInputResolverWithKeyResolver.class) String type,
                                         @Content @TypeResolver(TestInputResolverWithKeyResolver.class) Object content) {
    return null;
  }

  @org.mule.sdk.api.annotation.metadata.OutputResolver(output = SdkTestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object sdkContentMetadataWithKeyId(@Config Object object, @Connection MetadataConnection connection,
                                            @org.mule.sdk.api.annotation.metadata.MetadataKeyId(SdkTestInputResolverWithKeyResolver.class) String type,
                                            @Content @org.mule.sdk.api.annotation.metadata.TypeResolver(SdkTestInputResolverWithKeyResolver.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithKeyId(@Connection MetadataConnection connection,
                                        @MetadataKeyId(TestOutputResolverWithKeyResolver.class) String type,
                                        @Optional @Content Object content) {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object metadataKeyWithDefaultValue(@Connection MetadataConnection connection,
                                            @Optional(
                                                defaultValue = CAR) @MetadataKeyId(TestOutputResolverWithKeyResolver.class) String type,
                                            @Optional @Content Object content) {
    return type;
  }

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object metadataKeyOptional(@org.mule.sdk.api.annotation.param.Connection MetadataConnection connection,
                                    @Optional @MetadataKeyId(TestOutputResolverWithKeyResolver.class) String type,
                                    @Optional @Content Object content) {
    return type;
  }

  @OutputResolver(output = TestInputAndOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentAndOutputMetadataWithKeyId(@Connection MetadataConnection connection,
                                                  @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type,
                                                  @Content @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object content) {
    return null;
  }


  @OutputResolver(output = TestInputAndOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputAndMultipleInputWithKeyId(@Connection MetadataConnection connection,
                                                @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type,
                                                @Optional @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object firstPerson,
                                                @Optional @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object otherPerson) {
    return null;
  }


  @OutputResolver(output = TestInputAndOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputOnlyWithoutContentParam(@Connection MetadataConnection connection,
                                              @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type) {
    return type;
  }

  public boolean booleanMetadataKey(@Connection MetadataConnection connection, @MetadataKeyId boolean type,
                                    @Content @TypeResolver(TestBooleanMetadataResolver.class) Object content) {
    return type;
  }

  public AnimalClade enumMetadataKey(@Connection MetadataConnection connection, @MetadataKeyId AnimalClade type,
                                     @Content @TypeResolver(TestEnumMetadataResolver.class) Object content) {
    return type;
  }

  public void contentOnlyIgnoresOutput(@org.mule.sdk.api.annotation.param.Connection MetadataConnection connection,
                                       @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type,
                                       @Content @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object content) {}

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentMetadataWithoutKeyId(@org.mule.sdk.api.annotation.param.Connection MetadataConnection connection,
                                            @org.mule.sdk.api.annotation.param.Content @TypeResolver(TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithoutKeyId(@Connection MetadataConnection connection, @Optional @Content Object content) {
    return null;
  }

  @OutputResolver(output = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class)
  @MediaType(value = ANY, strict = false)
  public Object contentAndOutputMetadataWithoutKeyId(@Connection MetadataConnection connection,
                                                     @Content @TypeResolver(TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class) Object content) {
    return null;
  }

  public void contentMetadataWithoutKeysWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                                  @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) Object content) {}

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithoutKeysWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type) {
    return null;
  }

  @OutputResolver(output = TestResolverWithCache.class)
  @MediaType(value = ANY, strict = false)
  public Object contentAndOutputCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                              @Content @TypeResolver(TestResolverWithCache.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentOnlyCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                         @Content @TypeResolver(TestResolverWithCache.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestResolverWithCache.class)
  @MediaType(value = ANY, strict = false)
  public Object outputAndMetadataKeyCacheResolver(@Connection MetadataConnection connection,
                                                  @MetadataKeyId(TestResolverWithCache.class) String type) {
    return null;
  }

  public LocationKey simpleMultiLevelKeyResolver(@Connection MetadataConnection connection,
                                                 @ParameterGroup(
                                                     name = "key") @MetadataKeyId(TestMultiLevelKeyResolver.class) LocationKey locationKey,
                                                 @Content @TypeResolver(TestMultiLevelKeyResolver.class) Object content) {
    return locationKey;
  }

  public LocationKey partialMultiLevelKeyResolver(@Connection MetadataConnection connection,
                                                  @ParameterGroup(
                                                      name = "key") @MetadataKeyId(TestPartialMultiLevelKeyResolver.class) LocationKey locationKey,
                                                  @Content @TypeResolver(TestMultiLevelKeyResolver.class) Object content) {
    return locationKey;
  }

  public Order inputHasPojoWithExclusiveOptionalParameterGroup(Order dessertOrder) {
    return dessertOrder;
  }

  public Dessert inputHasExclusiveOptionalParameterGroup(@org.mule.sdk.api.annotation.param.ParameterGroup(
      name = "dessert") Dessert dessert) {
    return dessert;
  }

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Result<?, ?> messageAttributesAnyTypeMetadata() {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public Result<Object, StringAttributes> messageAttributesPersonTypeMetadata(@MetadataKeyId String type) {
    return null;
  }

  public void resolverContentWithContextClassLoader(@Content @TypeResolver(TestThreadContextClassLoaderResolver.class) Object content,
                                                    @MetadataKeyId(TestThreadContextClassLoaderResolver.class) String type) {}

  @OutputResolver(output = TestThreadContextClassLoaderResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object resolverOutputWithContextClassLoader(@MetadataKeyId String type) {
    return null;
  }

  @OutputResolver(output = TestOutputAttributesResolverWithKeyResolver.class,
      attributes = TestOutputAttributesResolverWithKeyResolver.class)
  public Result<Object, AbstractOutputAttributes> outputAttributesWithDynamicMetadata(
                                                                                      @MetadataKeyId(TestOutputAttributesResolverWithKeyResolver.class) String type) {
    return null;
  }

  @org.mule.sdk.api.annotation.metadata.OutputResolver(output = SdkTestOutputAttributesResolverWithKeyResolver.class,
      attributes = SdkTestOutputAttributesResolverWithKeyResolver.class)
  public Result<Object, AbstractOutputAttributes> sdkOutputAttributesWithDynamicMetadata(
                                                                                         @MetadataKeyId(SdkTestOutputAttributesResolverWithKeyResolver.class) String type) {
    return null;
  }

  public List<Result<String, StringAttributes>> listOfMessages() {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public List<Result<?, ?>> dynamicListOfMessages(@MetadataKeyId String type) {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public List<Object> dynamicListOfObjects(@MetadataKeyId String type, List<String> referableElements) {
    return null;
  }

  public boolean typeWithDeclaredSubtypesMetadata(Shape plainShape, Rectangle rectangleSubtype, Animal animal) {
    return false;
  }

  public void contentParameterShouldNotGenerateMapChildElement(
                                                               @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) Map<String, Object> mapContent) {}

  public void contentParameterShouldNotGenerateListChildElement(
                                                                @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) List<String> contents) {}

  public void contentParameterShouldNotGeneratePojoChildElement(
                                                                @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) Bear animalContent) {}

  @Query(translator = MetadataExtensionQueryTranslator.class,
      entityResolver = MetadataExtensionEntityResolver.class,
      nativeOutputResolver = NativeQueryOutputResolver.class)
  public List<Object> doQuery(@MetadataKeyId String query) {
    return singletonList(query);
  }

  @Query(translator = MetadataExtensionQueryTranslator.class,
      entityResolver = MetadataExtensionEntityResolver.class,
      nativeOutputResolver = NativeQueryOutputResolver.class)
  @MediaType(TEXT_PLAIN)
  public String returnQuery(@MetadataKeyId String query) {
    return query;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public PagingProvider<MetadataConnection, Result<Object, Animal>> pagedOperationResult(@MetadataKeyId String type) {
    return generateDummyPagingProvider();
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class, attributes = TestAttributesResolverWithKeyResolver.class)
  public PagingProvider<MetadataConnection, Result<Object, Object>> pagedOperationResultWithAttributesResolver(@MetadataKeyId String type) {
    return generateDummyPagingProvider();
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolver.class)
  public List<Object> objectListAsInput(@MetadataKeyId String type,
                                        @Optional @TypeResolver(TestInputResolverWithKeyResolver.class) List<Object> objects) {
    return null;
  }

  @MediaType(APPLICATION_JSON)
  public InputStream receiveJsonInputStream(@TypeResolver(AnyJsonTypeStaticResolver.class) @Content InputStream jsonValue) {
    return jsonValue;
  }

  public PagingProvider<MetadataConnection, Animal> pagedOperationMetadata(Animal animal) {
    return new PagingProvider<MetadataConnection, Animal>() {

      @Override
      public List<Animal> getPage(MetadataConnection connection) {
        return Collections.singletonList(animal);
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.of(1);
      }

      @Override
      public void close(MetadataConnection connection) throws MuleException {}
    };
  }

  public LocationKey partialMultiLevelKeyShowInDslResolver(@org.mule.sdk.api.annotation.param.Connection MetadataConnection connection,
                                                           @ParameterGroup(name = "keyShowInDsl",
                                                               showInDsl = true) @MetadataKeyId(TestPartialMultiLevelKeyResolver.class) LocationKey locationKeyShowInDslParam,
                                                           @Content @TypeResolver(TestMultiLevelKeyResolver.class) Object content) {
    return locationKeyShowInDslParam;
  }

  private <T> PagingProvider<MetadataConnection, T> generateDummyPagingProvider() {
    return new PagingProvider<MetadataConnection, T>() {

      @Override
      public List<T> getPage(MetadataConnection connection) {
        return emptyList();
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.empty();
      }

      @Override
      public void close(MetadataConnection connection) throws MuleException {}
    };
  }

  public Result<Shape, AbstractOutputAttributes> outputAttributesWithDeclaredSubtypesMetadata() {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolverUsingConfig.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithKeyIdUsingConfig(@Connection MetadataConnection connection,
                                                   @MetadataKeyId(TestOutputResolverWithKeyResolverUsingConfig.class) String type,
                                                   @Optional @Content Object content) {
    return null;
  }

  @MediaType(value = ANY, strict = false)
  public Object inputMetadataResolverInParameterInParameterGroup(@ParameterGroup(
      name = "Animal shelter") AnimalShelter animalShelter) {
    return null;
  }
}
