/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.metadata;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.mule.metadata.api.utils.MetadataTypeUtils.isCollection;
import static org.mule.metadata.api.utils.MetadataTypeUtils.isNullType;
import static org.mule.metadata.java.api.utils.JavaTypeUtils.getType;
import static org.mule.runtime.api.metadata.resolving.FailureCode.CONNECTION_FAILURE;
import static org.mule.runtime.api.metadata.resolving.FailureCode.NO_DYNAMIC_TYPE_AVAILABLE;
import static org.mule.runtime.api.metadata.resolving.MetadataFailure.Builder.newFailure;
import static org.mule.runtime.api.metadata.resolving.MetadataResult.failure;
import static org.mule.runtime.api.metadata.resolving.MetadataResult.success;
import static org.mule.runtime.module.extension.internal.metadata.MetadataResolverUtils.resolveWithOAuthRefresh;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.java.api.annotation.ClassInformationAnnotation;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.api.meta.model.EnrichableModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.descriptor.InputMetadataDescriptor;
import org.mule.runtime.api.metadata.descriptor.ParameterMetadataDescriptor;
import org.mule.runtime.api.metadata.descriptor.ParameterMetadataDescriptor.ParameterMetadataDescriptorBuilder;
import org.mule.runtime.api.metadata.descriptor.TypeMetadataDescriptor;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.MetadataFailure;
import org.mule.runtime.api.metadata.resolving.MetadataResult;
import org.mule.runtime.api.metadata.resolving.NamedTypeResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Metadata service delegate implementations that handles the resolution of a {@link ComponentModel}
 * {@link InputMetadataDescriptor}
 *
 * @since 4.0
 */
class MetadataInputDelegate extends BaseMetadataDelegate {

  MetadataInputDelegate(EnrichableModel model) {
    super(model);
  }

  /**
   * For each of the Component's {@link ParameterModel} creates the corresponding {@link TypeMetadataDescriptor} using only its
   * static {@link MetadataType} and ignoring if any parameter has a dynamic type.
   *
   * @return A {@link List} containing a {@link MetadataResult} of {@link TypeMetadataDescriptor} for each input parameter using
   *         only its static {@link MetadataType} and ignoring if any parameter has a dynamic type.
   */
  MetadataResult<InputMetadataDescriptor> getInputMetadataDescriptors(MetadataContext context, Object key) {
    InputMetadataDescriptor.InputMetadataDescriptorBuilder input = InputMetadataDescriptor.builder();
    List<MetadataResult<ParameterMetadataDescriptor>> results = new LinkedList<>();
    if (!(model instanceof ParameterizedModel)) {
      return failure(MetadataFailure.Builder.newFailure()
          .withMessage("The given component has not parameter definitions to be described").onComponent());
    }
    for (ParameterModel parameter : ((ParameterizedModel) model).getParameterGroupModels()
        .stream()
        .flatMap(parameterGroupModel -> parameterGroupModel.getParameterModels().stream())
        .collect(toList())) {
      MetadataResult<ParameterMetadataDescriptor> result = getParameterMetadataDescriptor(parameter, context, key);
      input.withParameter(parameter.getName(), result.get());
      results.add(result);
    }
    List<MetadataFailure> failures = results.stream().flatMap(e -> e.getFailures().stream()).collect(toList());
    return failures.isEmpty() ? success(input.build()) : failure(input.build(), failures);
  }

  /**
   * Given a parameters name, returns the associated {@link NamedTypeResolver}.
   * 
   * @param parameterName name of the parameter
   * @return {@link NamedTypeResolver} of the parameter
   */
  NamedTypeResolver getParameterResolver(String parameterName) {
    return resolverFactory.getInputResolver(parameterName);
  }

  /**
   * Creates a {@link TypeMetadataDescriptor} representing the Component's Content metadata using the {@link InputTypeResolver},
   * if one is available to resolve the {@link MetadataType}. If no the Component has no Content parameter, then
   * {@link Optional#empty()} is returned.
   *
   * @param context current {@link MetadataContext} that will be used by the {@link InputTypeResolver}
   * @param key     {@link MetadataKey} of the type which's structure has to be resolved
   * @return Success with an {@link Optional} {@link TypeMetadataDescriptor} representing the Component's Content metadata,
   *         resolved using the {@link InputTypeResolver} if one is available to resolve its {@link MetadataType}, returning
   *         {@link Optional#empty()} if no Content parameter is present Failure if the dynamic resolution fails for any reason.
   */
  private MetadataResult<ParameterMetadataDescriptor> getParameterMetadataDescriptor(ParameterModel parameter,
                                                                                     MetadataContext context, Object key) {

    ParameterMetadataDescriptorBuilder descriptorBuilder = ParameterMetadataDescriptor.builder(parameter.getName());
    if (!parameter.hasDynamicType()) {
      return success(descriptorBuilder.withType(parameter.getType()).build());
    }

    descriptorBuilder.dynamic(true);
    MetadataResult<MetadataType> inputMetadataResult = getParameterMetadata(parameter, context, key);
    MetadataType type = inputMetadataResult.get() == null ? parameter.getType() : inputMetadataResult.get();
    ParameterMetadataDescriptor descriptor = descriptorBuilder.withType(type).build();
    return inputMetadataResult.isSuccess() ? success(descriptor) : failure(descriptor, inputMetadataResult.getFailures());
  }

  /**
   * Given a {@link MetadataKey} of a type and a {@link MetadataContext}, resolves the {@link MetadataType} of the
   * {@code parameter} using the {@link InputTypeResolver} associated to the current component.
   *
   * @param context {@link MetadataContext} of the MetaData resolution
   * @param key     {@link MetadataKey} of the type which's structure has to be resolved
   * @return a {@link MetadataResult} with the {@link MetadataType} of the {@code parameter}.
   */
  private MetadataResult<MetadataType> getParameterMetadata(ParameterModel parameter, MetadataContext context, Object key) {
    try {
      boolean allowsNullType = !parameter.isRequired() && (parameter.getDefaultValue() == null);
      MetadataType metadata =
          resolveWithOAuthRefresh(context,
                                  () -> resolverFactory.getInputResolver(parameter.getName()).getInputMetadata(context, key));
      if (isMetadataResolvedCorrectly(metadata, allowsNullType)) {
        return success(adaptToListIfNecessary(metadata, parameter, context));
      }
      MetadataFailure failure = newFailure()
          .withMessage(format("Error resolving metadata for the [%s] input parameter", parameter.getName()))
          .withFailureCode(NO_DYNAMIC_TYPE_AVAILABLE)
          .withReason(NULL_TYPE_ERROR)
          .onParameter(parameter.getName());
      return failure(parameter.getType(), failure);
    } catch (ConnectionException e) {
      return failure(newFailure(e).withMessage("Failed to establish connection: " + ExceptionUtils.getMessage(e))
          .withFailureCode(CONNECTION_FAILURE).onComponent());
    } catch (Exception e) {
      return failure(parameter.getType(), newFailure(e).onParameter(parameter.getName()));
    }
  }

  private MetadataType adaptToListIfNecessary(MetadataType resolvedType, ParameterModel parameter,
                                              MetadataContext metadataContext) {

    MetadataType inputType = parameter.getType();
    if (!isCollection(inputType) || isNullType(resolvedType)) {
      return resolvedType;
    }
    return metadataContext.getTypeBuilder().arrayType()
        .with(new ClassInformationAnnotation(getType(inputType)))
        .of(resolvedType)
        .build();
  }
}
