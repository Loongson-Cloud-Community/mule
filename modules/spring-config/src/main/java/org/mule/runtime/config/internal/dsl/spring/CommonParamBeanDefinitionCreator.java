/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.dsl.spring;

import static org.mule.runtime.config.internal.dsl.spring.BeanDefinitionFactory.SPRING_PROTOTYPE_OBJECT;

import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.config.internal.dsl.model.SpringComponentModel;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinition;

import java.util.Map;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * Processor in the chain of responsibility that knows how to handle a generic {@code ComponentBuildingDefinition}.
 *
 * @since 4.0
 */
class CommonParamBeanDefinitionCreator extends CommonBeanBaseDefinitionCreator<CreateParamBeanDefinitionRequest> {

  public CommonParamBeanDefinitionCreator(ObjectFactoryClassRepository objectFactoryClassRepository,
                                          boolean disableTrimWhitespaces) {
    super(objectFactoryClassRepository, disableTrimWhitespaces);
  }

  @Override
  protected void processComponentDefinitionModel(Map<ComponentAst, SpringComponentModel> springComponentModels,
                                                 final CreateParamBeanDefinitionRequest request,
                                                 ComponentBuildingDefinition componentBuildingDefinition,
                                                 final BeanDefinitionBuilder beanDefinitionBuilder) {
    processObjectConstructionParameters(springComponentModels, request.resolveOwnerComponent(), null, request,
                                        new BeanDefinitionBuilderHelper(beanDefinitionBuilder));
    if (componentBuildingDefinition.isPrototype()) {
      beanDefinitionBuilder.setScope(SPRING_PROTOTYPE_OBJECT);
    }
    AbstractBeanDefinition originalBeanDefinition = beanDefinitionBuilder.getBeanDefinition();
    request.getSpringComponentModel().setBeanDefinition(originalBeanDefinition);
  }

}
