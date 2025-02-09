/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.privileged.processor;

import static java.util.Collections.emptyMap;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.core.api.processor.Processor;

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Marker interface that tells that a {@link Processor} is for internal use only.
 */
public interface InternalProcessor extends Component {

  @Override
  default Object getAnnotation(QName name) {
    return null;
  }

  @Override
  default Map<QName, Object> getAnnotations() {
    return emptyMap();
  }

  @Override
  default void setAnnotations(Map<QName, Object> annotations) {
    // Nothing to do
  }

  @Override
  default Location getRootContainerLocation() {
    return null;
  }

  @Override
  default ComponentLocation getLocation() {
    return null;
  }
}
