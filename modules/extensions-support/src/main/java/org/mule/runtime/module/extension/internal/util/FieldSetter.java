/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.util;

import java.lang.reflect.Field;

/**
 * Utility class which allows setting the value of a {@link Field} on random compatible instances
 *
 * @param <Target> the generic type of the objects which contain the field
 * @param <Value>  the field's generic type
 */
public final class FieldSetter<Target, Value> {

  /**
   * The {@link Field} in which the value is to be assigned
   */
  private final Field field;

  public FieldSetter(Field field) {
    this.field = field;
    field.setAccessible(true);
  }

  /**
   * Sets the {@code value} into the {@code target} instance
   *
   * @param target the object on which the field value is to be set
   * @param value  the value to set
   */
  public void set(Target target, Value value) {
    try {
      field.set(target, value);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
    }
  }

  /**
   * @return The {@link Field} to be set
   */
  public Field getField() {
    return field;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FieldSetter other = (FieldSetter) obj;
    if (field == null) {
      if (other.field != null) {
        return false;
      }
    } else if (!field.equals(other.field)) {
      return false;
    }
    return true;
  }


}
