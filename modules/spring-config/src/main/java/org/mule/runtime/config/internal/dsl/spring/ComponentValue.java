/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.dsl.spring;

import org.mule.runtime.api.component.ComponentIdentifier;

/**
 * Holder for the component identifier, it's bean value and the object {@code Class} that will be created from it.
 *
 * The bean value currently it's of an Object type since it can be a
 * {@link org.springframework.beans.factory.config.BeanDefinition} or a
 * {@link org.springframework.beans.factory.config.RuntimeBeanReference}
 *
 * @since 4.0
 */
class ComponentValue {

  private final Class<?> type;
  private final Object bean;
  private final ComponentIdentifier componentIdentifier;

  /**
   * @param componentIdentifier the identifier of the component for which the bean definition is provided
   * @param type                the type of the object to be created
   * @param bean                the bean definition
   */
  public ComponentValue(ComponentIdentifier componentIdentifier, Class<?> type, Object bean) {
    this.componentIdentifier = componentIdentifier;
    this.type = type;
    this.bean = bean;
  }

  /**
   * @return the object type that will be created from the bean value
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * @return the bean value definition. It may be a {@link org.springframework.beans.factory.config.RuntimeBeanReference} or a
   *         {@link org.springframework.beans.factory.config.BeanDefinition}
   */
  public Object getBean() {
    return bean;
  }

  /**
   * @return the identifier of the component model associated to the configuration.
   */
  public ComponentIdentifier getComponentIdentifier() {
    return componentIdentifier;
  }
}
