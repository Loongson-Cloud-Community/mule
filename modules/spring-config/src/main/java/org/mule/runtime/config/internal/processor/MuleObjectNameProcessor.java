/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.processor;

import org.mule.runtime.api.meta.NameableObject;
import org.mule.runtime.core.api.transformer.Transformer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * <code>MuleObjectNameProcessor</code> is used to set spring ids to Mule object names so the the bean id and name property on the
 * object don't both have to be set.
 */

public class MuleObjectNameProcessor implements BeanPostProcessor {

  private boolean overwrite = false;
  private final Class<? extends NameableObject> managedTypes[] = new Class[] {Transformer.class};

  @Override
  public Object postProcessBeforeInitialization(Object object, String beanName) throws BeansException {
    for (Class<? extends NameableObject> managedType : managedTypes) {
      if (managedType.isInstance(object)) {
        setNameIfNecessary((NameableObject) object, beanName);
      }
    }

    return object;
  }

  private void setNameIfNecessary(NameableObject nameable, String name) {
    if (nameable.getName() == null || overwrite) {
      nameable.setName(name);
    }
  }

  @Override
  public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
    return o;
  }

  public boolean isOverwrite() {
    return overwrite;
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

}
