/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.processor;

import org.mule.api.annotation.NoImplement;
import org.mule.runtime.api.component.location.ComponentLocation;

/**
 * An interface that indicates that the {@link ComponentLocation} can be resolved. It is used as a WA so that the subtypes of
 * {@link ReactiveProcessor} that are associated to a location can be identified and a location can be obtained for those types.
 *
 * // TODO MULE-19594: refactor the way of retrieving the component location from a generic reactive processor.
 */
@NoImplement
public interface HasLocation {

  /**
   * @return the resolved {@link ComponentLocation}.
   */
  public ComponentLocation resolveLocation();

}
