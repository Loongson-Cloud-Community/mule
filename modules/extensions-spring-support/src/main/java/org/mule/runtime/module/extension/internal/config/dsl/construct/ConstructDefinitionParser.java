/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.config.dsl.construct;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.construct.ConstructModel;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import org.mule.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import org.mule.runtime.module.extension.internal.parser.AbstractComponentDefinitionParser;
import org.mule.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import org.mule.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import org.mule.runtime.module.extension.internal.runtime.operation.ConstructMessageProcessor;

/**
 * A {@link ExtensionDefinitionParser} for parsing {@link ConstructMessageProcessor} instances through a
 * {@link ConstructMessageProcessorObjectFactory}
 *
 * @since 4.0
 */
public class ConstructDefinitionParser extends AbstractComponentDefinitionParser<ConstructModel> {

  public ConstructDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                   ConstructModel constructModel,
                                   DslSyntaxResolver dslSyntaxResolver,
                                   ExtensionParsingContext parsingContext) {
    super(definition, extensionModel, constructModel, dslSyntaxResolver, parsingContext);
  }

  @Override
  protected Class<ConstructMessageProcessor> getMessageProcessorType() {
    return ConstructMessageProcessor.class;
  }

  @Override
  protected Class<ConstructMessageProcessorObjectFactory> getMessageProcessorFactoryType() {
    return ConstructMessageProcessorObjectFactory.class;
  }
}
