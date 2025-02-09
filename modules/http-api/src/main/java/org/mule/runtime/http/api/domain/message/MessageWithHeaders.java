/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.domain.message;

import org.mule.runtime.api.util.MultiMap;

import java.util.Collection;

/**
 * Generic contract for a message which contains headers
 *
 * @since 4.2.0
 */
public interface MessageWithHeaders {

  /**
   * @return all headers name
   */
  Collection<String> getHeaderNames();

  /**
   * @param headerName name of the header
   * @return whether or not the header is present in the message
   * @since 1.2
   */
  default boolean containsHeader(String headerName) {
    return getHeaderValue(headerName) != null;
  }

  /**
   * @param headerName name of the header
   * @return first value of the header
   */
  String getHeaderValue(String headerName);

  /**
   * @param headerName name of the header
   * @return first value of the header, regardless of the case
   *
   * @deprecated The underlying implementation is already case-insensitive. Use {@link #getHeaderValue(String)}
   */
  @Deprecated
  String getHeaderValueIgnoreCase(String headerName);

  /**
   * @param headerName name of the header
   * @return an immutable {@link Collection} containing all the values of that headers. If not such headers exists return null,
   *         otherwise the collection of header values
   */
  Collection<String> getHeaderValues(String headerName);

  /**
   * @param headerName name of the header
   * @return an immutable {@link Collection} containing all the values of that headers, regardless of the case. If not such
   *         headers exists return null, otherwise the collection of header values
   * @deprecated The underlying implementation is already case-insensitive. Use {@link #getHeaderValues(String)}
   */
  @Deprecated
  Collection<String> getHeaderValuesIgnoreCase(String headerName);

  /**
   * @return an immutable {@link MultiMap} containing all headers
   */
  MultiMap<String, String> getHeaders();
}
