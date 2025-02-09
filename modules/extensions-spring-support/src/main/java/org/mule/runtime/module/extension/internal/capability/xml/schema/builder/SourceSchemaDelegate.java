/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.capability.xml.schema.builder;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.mule.runtime.module.extension.internal.config.dsl.SchemaConstants.MULE_ABSTRACT_MESSAGE_SOURCE;
import static org.mule.runtime.module.extension.internal.config.dsl.SchemaConstants.MULE_ABSTRACT_MESSAGE_SOURCE_TYPE;
import static org.mule.runtime.module.extension.internal.config.dsl.SchemaConstants.TYPE_SUFFIX;

import org.mule.runtime.api.meta.model.parameter.ParameterGroupModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.api.meta.model.source.SourceModel;
import org.mule.runtime.extension.api.dsl.syntax.DslElementSyntax;
import org.mule.runtime.module.extension.internal.capability.xml.schema.model.Element;
import org.mule.runtime.module.extension.internal.capability.xml.schema.model.ExplicitGroup;
import org.mule.runtime.module.extension.internal.capability.xml.schema.model.ExtensionType;
import org.mule.runtime.module.extension.internal.capability.xml.schema.model.TopLevelElement;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Builder delegation class to generate a XSD schema that describes a {@link SourceModel}
 *
 * @since 4.0.0
 */
class SourceSchemaDelegate extends ExecutableTypeSchemaDelegate {

  SourceSchemaDelegate(SchemaBuilder builder) {
    super(builder);
  }

  void registerMessageSource(SourceModel sourceModel, DslElementSyntax dslSyntax, boolean hasImplicitConfig) {
    String typeName = capitalize(sourceModel.getName()) + TYPE_SUFFIX;
    registerSourceElement(sourceModel, typeName, dslSyntax);
    registerSourceType(typeName, sourceModel, dslSyntax, hasImplicitConfig);
  }

  private void registerSourceElement(SourceModel sourceModel, String typeName, DslElementSyntax dslSyntax) {
    Element element = new TopLevelElement();
    element.setName(dslSyntax.getElementName());
    element.setType(new QName(builder.getSchema().getTargetNamespace(), typeName));
    element.setAnnotation(builder.createDocAnnotation(sourceModel.getDescription()));
    element.setSubstitutionGroup(getSourceSubstitutionGroup(sourceModel));
    builder.getSchema().getSimpleTypeOrComplexTypeOrGroup().add(element);
  }

  private QName getSourceSubstitutionGroup(SourceModel sourceModel) {
    return getSubstitutionGroup(sourceModel, MULE_ABSTRACT_MESSAGE_SOURCE);
  }

  private void registerSourceType(String name, SourceModel sourceModel, DslElementSyntax dslSyntax, boolean hasImplicitConfig) {
    final ExtensionType sourceType = createExecutableType(name, MULE_ABSTRACT_MESSAGE_SOURCE_TYPE, dslSyntax, hasImplicitConfig);
    initialiseSequence(sourceType);
    ExplicitGroup sequence = sourceType.getSequence();
    builder.addInfrastructureParameters(sourceType, sourceModel, sequence);

    List<ParameterModel> visitedParameters = new LinkedList<>();
    sourceModel.getParameterGroupModels().forEach(group -> {
      registerParameterGroup(sourceType, group);
      visitedParameters.addAll(group.getParameterModels());
    });

    sourceModel.getSuccessCallback().map(ParameterizedModel::getParameterGroupModels)
        .ifPresent(groups -> groups
            .forEach(group -> registerCallbackParameters(sourceType, visitedParameters, group)));

    sourceModel.getErrorCallback().map(ParameterizedModel::getParameterGroupModels)
        .ifPresent(groups -> groups
            .forEach(group -> registerCallbackParameters(sourceType, visitedParameters, group)));
  }

  private void registerCallbackParameters(ExtensionType sourceType, List<ParameterModel> visitedParameters,
                                          ParameterGroupModel group) {
    if (group.isShowInDsl()) {
      builder.addInlineParameterGroup(group, sourceType.getSequence());
    } else {
      List<ParameterModel> callbackParameters = group.getParameterModels().stream()
          .filter(p -> !visitedParameters.contains(p)).collect(toList());

      registerParameters(sourceType, callbackParameters);
      visitedParameters.addAll(group.getParameterModels());
    }
  }

}
