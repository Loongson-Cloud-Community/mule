/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.processor;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.config.internal.context.SpringConfigurationComponentLocator;
import org.mule.runtime.dsl.api.component.ComponentFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Bean post processor that creates an {@link SpringConfigurationComponentLocator}.
 *
 * For each registered bean, it checks if it's a configuration components and in such case it adds the component to the
 * {@link SpringConfigurationComponentLocator} instance.
 *
 * @since 4.0
 */
public class ComponentLocatorCreatePostProcessor implements BeanPostProcessor {

  private SpringConfigurationComponentLocator componentLocator;

  /**
   * Creates a new instance that will populate component over {@code componentLocator}
   *
   * @param componentLocator the locator in which configuration components must be added.
   */
  public ComponentLocatorCreatePostProcessor(SpringConfigurationComponentLocator componentLocator) {
    this.componentLocator = componentLocator;
  }

  @Override
  public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
    return o;
  }

  @Override
  public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
    // We check for instanceof Component because this method may be called for spring objects
    if ((!(o instanceof FactoryBean) || o instanceof ComponentFactory) && o instanceof Component
        && ((Component) o).getLocation() != null) {
      componentLocator.addComponent((Component) o);
    }
    return o;
  }
}
