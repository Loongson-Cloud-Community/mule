/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.processor;

import org.mule.runtime.config.internal.registry.OptionalObjectsController;

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * A {@link InstantiationAwareBeanPostProcessor} which suspends initialization and population of discarded objects and removes
 * them from the registry
 *
 * @since 3.7.0
 */
// TODO W-10736276 Remove this
public class DiscardedOptionalBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

  private final OptionalObjectsController optionalObjectsController;
  private final DefaultListableBeanFactory beanFactory;

  public DiscardedOptionalBeanPostProcessor(OptionalObjectsController optionalObjectsController,
                                            DefaultListableBeanFactory beanFactory) {
    this.optionalObjectsController = optionalObjectsController;
    this.beanFactory = beanFactory;
  }

  @Override
  public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
    return null;
  }

  @Override
  public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
    return !optionalObjectsController.isDiscarded(beanName);
  }

  @Override
  public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
      throws BeansException {
    return optionalObjectsController.isDiscarded(beanName) ? null : pvs;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (optionalObjectsController.isDiscarded(beanName)) {
      if (beanFactory.containsBeanDefinition(beanName)) {
        beanFactory.removeBeanDefinition(beanName);
      }

      beanFactory.destroySingleton(beanName);
      return null;
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }
}
