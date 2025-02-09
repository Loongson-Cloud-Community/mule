/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.domain.message.request;

import static java.util.Objects.requireNonNull;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.runtime.http.api.domain.HttpProtocol.HTTP_1_1;
import static org.mule.runtime.http.api.utils.UriCache.getUriFromString;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.util.MultiMap.StringMultiMap;
import org.mule.runtime.http.api.HttpConstants.Method;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.runtime.http.api.domain.HttpProtocol;
import org.mule.runtime.http.api.domain.message.HttpMessage;
import org.mule.runtime.http.api.domain.message.HttpMessageBuilder;

import java.net.URI;

/**
 * Builder of {@link HttpRequest}s. Instances can only be obtained using {@link HttpRequest#builder()}. At the very least, the
 * request URI needs to be provided via {@link #uri(String)}. By default, GET is used as method with empty headers, query params
 * and entity.
 *
 * @since 4.0
 */
public final class HttpRequestBuilder extends HttpMessageBuilder<HttpRequestBuilder, HttpRequest> {

  private String path;
  private URI uri;
  private String method = GET.name();
  private MultiMap<String, String> queryParams = new StringMultiMap();
  private HttpProtocol protocol = HTTP_1_1;

  HttpRequestBuilder(boolean preserveHeadersCase) {
    headers = new CaseInsensitiveMultiMap(!preserveHeadersCase);
  }

  /**
   * Declares the URI where this {@link HttpRequest} will be sent. Minimum required configuration.
   *
   * @param uri the URI (as a String) of the {@link HttpRequest} desired. Non null.
   * @return this builder
   */
  public HttpRequestBuilder uri(String uri) {
    return uri(getUriFromString(uri));
  }

  /**
   * Declares the URI where this {@link HttpRequest} will be sent. Minimum required configuration.
   *
   * @param uri the URI of the {@link HttpRequest} desired. Non null.
   * @return this builder
   */
  public HttpRequestBuilder uri(URI uri) {
    this.uri = uri;
    this.path = uri.getPath();
    return this;
  }

  /**
   * Allows for using extension methods, as defined in the rfc. In general, {@link #method(Method)} should be used.
   *
   * @param method the HTTP method of the {@link HttpRequest} desired. Non null.
   * @return this builder
   */
  public HttpRequestBuilder method(String method) {
    this.method = method;
    return this;
  }

  /**
   * @param method the HTTP method of the {@link HttpRequest} desired. Non null.
   * @return this builder
   */
  public HttpRequestBuilder method(Method method) {
    this.method = method.name();
    return this;
  }

  /**
   * @param protocol the HTTP protocol of the {@link HttpRequest} desired. Non null.
   * @return this builder
   * @since 4.2.0
   */
  public HttpRequestBuilder protocol(HttpProtocol protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * @param queryParams a {@link MultiMap} representing the HTTP query params of the {@link HttpRequest} desired. Non null.
   * @return this builder
   */
  public HttpRequestBuilder queryParams(MultiMap<String, String> queryParams) {
    this.queryParams.putAll(queryParams);
    return this;
  }

  /**
   * Includes a new queryParam to be sent in the desired {@link HttpMessage}.
   *
   * @param name  the name of the HTTP queryParam
   * @param value the value of the HTTP queryParam
   * @return this builder
   */
  public HttpRequestBuilder addQueryParam(String name, String value) {
    this.queryParams.put(name, value);
    return this;
  }

  /**
   * @return the current URI configured in the builder.
   */
  public URI getUri() {
    return uri;
  }

  /**
   * @return the current HTTP method configured in the builder.
   */
  public String getMethod() {
    return method;
  }

  /**
   * @return an immutable version of the current query parameters in the builder.
   */
  public MultiMap<String, String> getQueryParams() {
    return queryParams.toImmutableMultiMap();
  }

  /**
   * Discard this builder after calling this method.
   *
   * @return an {@link HttpRequest} as described.
   */
  @Override
  public HttpRequest build() {
    requireNonNull(uri, "URI must be specified to create an HTTP request");
    return new DefaultHttpRequest(uri, path, method, protocol, headers, queryParams, entity);

  }

}
