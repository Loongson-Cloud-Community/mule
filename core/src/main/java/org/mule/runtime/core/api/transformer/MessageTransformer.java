/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.transformer;

import org.mule.runtime.core.api.event.CoreEvent;

import java.nio.charset.Charset;

/**
 * A transformer intended to transform Mule messages rather than arbitrary objects
 */
public interface MessageTransformer extends Transformer {

  /**
   * Transforms the supplied data and returns the result
   *
   * @param src   the data to transform
   * @param event the event currently being processed
   * @return the transformed data
   * @throws MessageTransformerException if a error occurs transforming the data or if the expected returnClass isn't the same as
   *                                     the transformed data
   */
  Object transform(Object src, CoreEvent event) throws MessageTransformerException;

  /**
   * Transforms the supplied data and returns the result
   *
   * @param src      the data to transform
   * @param encoding the encoding to use by this transformer. many transformations will not need encoding unless dealing with text
   *                 so you only need to use this method if yo wish to customize the encoding
   * @param event    the event currently being processed
   * @return the transformed data
   * @throws MessageTransformerException if a error occurs transforming the data or if the expected returnClass isn't the same as
   *                                     the transformed data
   */
  Object transform(Object src, Charset encoding, CoreEvent event) throws MessageTransformerException;
}
