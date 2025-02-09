/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.util;

import org.mule.runtime.api.meta.NameableObject;
import org.mule.runtime.core.api.config.i18n.CoreMessages;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.core.api.util.StringUtils;
import org.mule.runtime.core.api.util.UUID;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>BeanUtils</code> provides functions for altering the way commons BeanUtils works
 * 
 * @deprecated this will be removed in next major version
 */
// @ThreadSafe
@Deprecated
public class BeanUtils {

  public static final String SET_PROPERTIES_METHOD = "setProperties";

  /**
   * logger used by this class
   */
  private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

  /**
   * Exception safe version of BeanUtils.populate()
   *
   * @param object      the object to set the properties on
   * @param props       the map of properties to set
   * @param logWarnings whether exception warnings should be logged
   */
  public static void populateWithoutFail(Object object, Map props, boolean logWarnings) {
    // Check to see if our object has a setProperties method where the properties
    // map should be set
    if (ClassUtils.getMethod(object.getClass(), SET_PROPERTIES_METHOD, new Class[] {Map.class}) != null) {
      try {
        org.apache.commons.beanutils.BeanUtils.setProperty(object, "properties", props);
      } catch (Exception e) {
        // this should never happen since we explicitly check for the method
        // above
        if (logWarnings) {
          logger.warn("Property: " + SET_PROPERTIES_METHOD + "=" + Map.class.getName() + " not found on object: "
              + object.getClass().getName());
        }
      }
    } else {
      for (Object element : props.entrySet()) {
        Map.Entry entry = (Map.Entry) element;

        try {
          org.apache.commons.beanutils.BeanUtils.setProperty(object, entry.getKey().toString(), entry.getValue());
        } catch (Exception e) {
          if (logWarnings) {
            logger.warn("Property: " + entry.getKey() + "=" + entry.getValue() + " not found on object: "
                + object.getClass().getName());
          }
        }
      }
    }
  }

  /**
   * This will overlay a map of properties on a bean. This method will validate that all properties are available on the bean
   * before setting the properties
   *
   * @param bean  the bean on which to set the properties
   * @param props a Map of properties to set on the bean
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void populate(Object bean, Map props) throws IllegalAccessException, InvocationTargetException {
    // Check to see if our object has a setProperties method where the properties
    // map should be set
    if (ClassUtils.getMethod(bean.getClass(), SET_PROPERTIES_METHOD, new Class[] {Map.class}) != null) {
      org.apache.commons.beanutils.BeanUtils.setProperty(bean, "properties", props);
    } else {
      Map mainProps = describe(bean);
      for (Object o : props.keySet()) {
        if (!mainProps.containsKey(o)) {
          throw new IllegalArgumentException(CoreMessages.propertyDoesNotExistOnObject(o.toString(), bean).getMessage());
        }

      }
      org.apache.commons.beanutils.BeanUtils.populate(bean, props);
    }
  }

  /**
   * The Apache BeanUtils version of this converts all values to String, which is pretty useless, it also includes stuff not
   * defined by the user
   *
   * @param object the object to Describe
   * @return a map of the properties on the object
   */
  private static Map describe(Object object) {
    Map props = new HashMap(object.getClass().getDeclaredFields().length);
    for (int i = 0; i < object.getClass().getDeclaredFields().length; i++) {
      Field field = object.getClass().getDeclaredFields()[i];
      field.setAccessible(true);
      try {
        props.put(field.getName(), field.get(object));
      } catch (IllegalAccessException e) {
        logger.debug("Unable to read field: " + field.getName() + " on object: " + object);
      }
    }
    return props;
  }

  /**
   * Returns the name for the object passed in. If the object implements {@link NameableObject}, then
   * {@link NameableObject#getName()} will be returned, otherwise a name is generated using the class name and a generated UUID.
   *
   * @param obj the object to inspect
   * @return the name for this object
   */
  public static String getName(Object obj) {
    String name = null;
    if (obj instanceof NameableObject) {
      name = ((NameableObject) obj).getName();
    } else if (obj instanceof FlowConstruct) {
      name = ((FlowConstruct) obj).getName();
    }
    if (StringUtils.isBlank(name)) {
      name = obj.getClass().getName() + ":" + UUID.getUUID();
    }
    return name;
  }

  private BeanUtils() {}
}
