/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.config.dsl.source;

import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static org.mule.runtime.dsl.api.component.TypeDefinition.fromType;
import static org.mule.runtime.extension.api.ExtensionConstants.BACK_PRESSURE_STRATEGY_PARAMETER_NAME;
import static org.mule.runtime.extension.api.ExtensionConstants.PRIMARY_NODE_ONLY_PARAMETER_NAME;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.toBackPressureStrategy;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.parameter.ParameterGroupModel;
import org.mule.runtime.api.meta.model.source.SourceModel;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.retry.policy.RetryPolicyTemplate;
import org.mule.runtime.core.api.streaming.CursorProviderFactory;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import org.mule.runtime.extension.api.dsl.syntax.DslElementSyntax;
import org.mule.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import org.mule.runtime.extension.api.property.BackPressureStrategyModelProperty;
import org.mule.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import org.mule.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import org.mule.runtime.module.extension.internal.runtime.source.ExtensionMessageSource;

import java.util.List;

/**
 * An {@link ExtensionMessageSource} used to parse instances of {@link ExtensionMessageSource} instances through a
 * {@link SourceDefinitionParser}
 *
 * @since 4.0
 */
public class SourceDefinitionParser extends ExtensionDefinitionParser {

  private final ExtensionModel extensionModel;
  private final SourceModel sourceModel;
  private final DslElementSyntax sourceDsl;

  public SourceDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                SourceModel sourceModel, DslSyntaxResolver dslSyntaxResolver,
                                ExtensionParsingContext parsingContext) {
    super(definition, dslSyntaxResolver, parsingContext);
    this.extensionModel = extensionModel;
    this.sourceModel = sourceModel;
    this.sourceDsl = dslSyntaxResolver.resolve(sourceModel);
  }

  @Override
  protected Builder doParse(Builder definitionBuilder) throws ConfigurationException {
    Builder finalBuilder =
        definitionBuilder.withIdentifier(sourceDsl.getElementName()).withTypeDefinition(fromType(ExtensionMessageSource.class))
            .withObjectFactoryType(ExtensionSourceObjectFactory.class)
            .withConstructorParameterDefinition(fromFixedValue(extensionModel).build())
            .withConstructorParameterDefinition(fromFixedValue(sourceModel).build())
            .withConstructorParameterDefinition(fromReferenceObject(MuleContext.class).build())
            .withSetterParameterDefinition("retryPolicyTemplate", fromChildConfiguration(RetryPolicyTemplate.class).build())
            .withSetterParameterDefinition(CURSOR_PROVIDER_FACTORY_FIELD_NAME,
                                           fromChildConfiguration(CursorProviderFactory.class).build());

    if (!sourceModel.runsOnPrimaryNodeOnly()) {
      finalBuilder = finalBuilder
          .withSetterParameterDefinition("primaryNodeOnly", fromSimpleParameter(PRIMARY_NODE_ONLY_PARAMETER_NAME).build());
    }

    if (sourceModel.getModelProperty(BackPressureStrategyModelProperty.class)
        .filter(bpmp -> bpmp.getSupportedModes().size() > 1)
        .isPresent()) {
      finalBuilder = finalBuilder
          .withSetterParameterDefinition("backPressureStrategy",
                                         fromSimpleParameter(BACK_PRESSURE_STRATEGY_PARAMETER_NAME,
                                                             v -> toBackPressureStrategy((String) v)).build());
    }

    List<ParameterGroupModel> inlineGroups = getInlineGroups(sourceModel);
    sourceModel.getErrorCallback().ifPresent(cb -> inlineGroups.addAll(getInlineGroups(cb)));
    sourceModel.getSuccessCallback().ifPresent(cb -> inlineGroups.addAll(getInlineGroups(cb)));

    parseParameters(getFlatParameters(inlineGroups, sourceModel.getAllParameterModels()));

    for (ParameterGroupModel group : inlineGroups) {
      parseInlineParameterGroup(group);
    }

    return finalBuilder;
  }
}
