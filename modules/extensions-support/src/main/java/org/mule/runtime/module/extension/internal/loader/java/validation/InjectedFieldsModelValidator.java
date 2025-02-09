/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.metadata.java.api.utils.JavaTypeUtils.getType;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.getAnnotatedFields;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.getImplementingType;

import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.visitor.MetadataTypeVisitor;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.NamedObject;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.connection.ConnectionProviderModel;
import org.mule.runtime.api.meta.model.connection.HasConnectionProviderModels;
import org.mule.runtime.api.meta.model.operation.HasOperationModels;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterGroupModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.api.meta.model.source.HasSourceModels;
import org.mule.runtime.api.meta.model.source.SourceModel;
import org.mule.runtime.api.meta.model.util.ExtensionWalker;
import org.mule.runtime.extension.api.annotation.param.DefaultEncoding;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.declaration.type.annotation.InfrastructureTypeAnnotation;
import org.mule.runtime.extension.api.loader.ExtensionModelValidator;
import org.mule.runtime.extension.api.loader.Problem;
import org.mule.runtime.extension.api.loader.ProblemsReporter;
import org.mule.runtime.extension.api.property.ClassLoaderModelProperty;
import org.mule.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import org.mule.runtime.module.extension.api.loader.java.type.MethodElement;
import org.mule.runtime.module.extension.internal.loader.java.type.property.ExtensionOperationDescriptorModelProperty;
import org.mule.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import org.mule.sdk.api.annotation.param.RuntimeVersion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validates that the fields which are annotated with {@link RefName} or {@link DefaultEncoding} honor that:
 * <ul>
 * <li>The annotated field is of {@link String} type</li>
 * <li>There is at most one field annotated per type</li>
 * </ul>
 * <p>
 * It also validates the aforementioned rules for all the {@link OperationModel} method's arguments.
 *
 * @since 4.0
 */
public final class InjectedFieldsModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    final Set<Class<?>> validatedTypes = new HashSet<>();
    // TODO - MULE-14401 - Make InjectedFieldsModelValidator work in AST Mode
    boolean isASTMode = !extensionModel.getModelProperty(ExtensionTypeDescriptorModelProperty.class)
        .map(mp -> mp.getType().getDeclaringClass().isPresent())
        .orElse(false);

    if (!isASTMode) {
      extensionModel.getModelProperty(ClassLoaderModelProperty.class).ifPresent(classLoaderModelProperty -> {
        new ExtensionWalker() {

          @Override
          protected void onSource(HasSourceModels owner, SourceModel model) {
            Optional<Class> implementingType = getImplementingType(model);
            validateFields(model, implementingType, String.class, DefaultEncoding.class,
                           org.mule.sdk.api.annotation.param.DefaultEncoding.class);
            validateFields(model, implementingType, MuleVersion.class, RuntimeVersion.class);
          }

          @Override
          protected void onConfiguration(ConfigurationModel model) {
            Optional<Class> implementingType = getImplementingType(model);
            validateFields(model, implementingType, String.class, DefaultEncoding.class,
                           org.mule.sdk.api.annotation.param.DefaultEncoding.class);
            validateFields(model, implementingType, String.class, RefName.class,
                           org.mule.sdk.api.annotation.param.RefName.class);
            validateFields(model, implementingType, MuleVersion.class, RuntimeVersion.class);
          }

          @Override
          protected void onOperation(HasOperationModels owner, OperationModel model) {
            validateArguments(model, model.getModelProperty(ExtensionOperationDescriptorModelProperty.class),
                              DefaultEncoding.class, org.mule.sdk.api.annotation.param.DefaultEncoding.class);
          }

          @Override
          protected void onConnectionProvider(HasConnectionProviderModels owner, ConnectionProviderModel model) {
            Optional<Class> implementingType = getImplementingType(model);
            validateFields(model, getImplementingType(model), String.class, DefaultEncoding.class,
                           org.mule.sdk.api.annotation.param.DefaultEncoding.class);
            validateFields(model, getImplementingType(model), String.class, RefName.class,
                           org.mule.sdk.api.annotation.param.RefName.class);
            validateFields(model, implementingType, MuleVersion.class, RuntimeVersion.class);

          }

          @Override
          protected void onParameter(ParameterizedModel owner, ParameterGroupModel groupModel, ParameterModel model) {
            if (model.getType().getMetadataFormat().equals(JAVA)) {
              model.getType().accept(new MetadataTypeVisitor() {

                @Override
                public void visitObject(ObjectType objectType) {
                  if (!objectType.getAnnotation(InfrastructureTypeAnnotation.class).isPresent()) {
                    try {
                      Class<?> type = getType(objectType, classLoaderModelProperty.getClassLoader());
                      if (validatedTypes.add(type)) {
                        validateType(model, type, String.class, DefaultEncoding.class,
                                     org.mule.sdk.api.annotation.param.DefaultEncoding.class);
                      }
                    } catch (Exception e) {
                      problemsReporter.addWarning(new Problem(model, "Could not validate Class: " + e.getMessage()));
                    }
                  }
                }
              });
            }
          }

          private void validateArguments(NamedObject model, Optional<ExtensionOperationDescriptorModelProperty> modelProperty,
                                         Class<? extends Annotation>... annotationClasses) {
            modelProperty.ifPresent(operationDescriptorModelProperty -> {
              MethodElement operation = operationDescriptorModelProperty.getOperationElement();
              List<ExtensionParameter> annotatedExtensionParameters = new ArrayList<>();
              of(annotationClasses).forEach(annotationClass -> annotatedExtensionParameters
                  .addAll(operation.getParametersAnnotatedWith(annotationClass)));
              int size = annotatedExtensionParameters.size();
              if (size == 0) {
                return;
              } else if (size > 1) {
                problemsReporter
                    .addError(new Problem(model,
                                          format("Operation method '%s' has %d arguments annotated with [@%s]. Only one argument may carry that annotation",
                                                 operation.getName(), size,
                                                 of(annotationClasses).map(annotationClass -> annotationClass.getName())
                                                     .collect(joining(", @")))));
              }

              ExtensionParameter argument = annotatedExtensionParameters.get(0);
              if (!argument.getType().isSameType(String.class)) {
                problemsReporter
                    .addError(new Problem(model,
                                          format("Operation method '%s' declares an argument '%s' which is annotated with [@%s] and is of type '%s'. Only "
                                              + "arguments of type String are allowed to carry such annotation",
                                                 operation.getName(),
                                                 argument.getName(), of(annotationClasses)
                                                     .map(annotationClass -> annotationClass.getName()).collect(joining(", @")),
                                                 argument.getType().getName())));
              }
            });
          }

          private void validateFields(NamedObject model, Optional<Class> implementingType, Class implementingClass,
                                      Class<? extends Annotation>... annotationClasses) {
            implementingType.ifPresent(type -> validateType(model, type, implementingClass, annotationClasses));
          }

          private void validateType(NamedObject model, Class<?> type, Class implementingClass,
                                    Class<? extends Annotation>... annotationClasses) {
            List<Field> fields = getAnnotatedFields(type, annotationClasses);
            if (fields.isEmpty()) {
              return;
            } else if (fields.size() > 1) {
              problemsReporter
                  .addError(new Problem(model,
                                        format("Class '%s' has %d fields annotated with one of [@%s]. Only one field may carry that annotation",
                                               type.getName(), fields.size(), of(annotationClasses)
                                                   .map(annotationClass -> annotationClass.getName()).collect(joining(", @")))));
            }

            Field field = fields.get(0);
            if (!implementingClass.equals(field.getType())) {
              problemsReporter
                  .addError(new Problem(model,
                                        format("Class '%s' declares the field '%s' which is annotated with one of [@%s] and is of type '%s'. Only "
                                            + "fields of type String are allowed to carry such annotation", type.getName(),
                                               field.getName(), of(annotationClasses)
                                                   .map(annotationClass -> annotationClass.getName()).collect(joining(", @")),
                                               field.getType().getName())));
            }
          }
        }.walk(extensionModel);
      });
    }
  }
}
