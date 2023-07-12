/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.tracer.api.span.info;

/**
 * The Component Span information.
 *
 * @since 4.5.0
 */
public interface ComponentSpanInfo {

  /**
   * @return the initial name for the span.
   */
  String getName();

  /**
   * @return the initial span info generated for the span.
   */
  InitialSpanInfo getInitialSpanInfo();
}
