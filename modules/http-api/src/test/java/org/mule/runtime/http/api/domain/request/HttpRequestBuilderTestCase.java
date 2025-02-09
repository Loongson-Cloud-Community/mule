/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.domain.request;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;
import static org.mule.runtime.http.api.utils.UriCache.getUriFromString;
import static org.mule.test.allure.AllureConstants.HttpFeature.HTTP_SERVICE;
import static org.mule.test.allure.AllureConstants.HttpFeature.HttpStory.REQUEST_BUILDER;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.net.URI;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(HTTP_SERVICE)
@Story(REQUEST_BUILDER)
public class HttpRequestBuilderTestCase {

  public static final String URI_VALUE = "someUri";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private HttpRequestBuilder builder = HttpRequest.builder();
  private String name = "name";
  private String value = "value";

  @Test
  public void defaultRequest() {
    URI uri = getUriFromString("someUri");
    HttpRequest request = builder.uri(uri).build();
    assertThat(request.getMethod(), is("GET"));
    assertThat(request.getUri(), is(uri));
    assertThat(request.getEntity(), is(instanceOf(EmptyHttpEntity.class)));
    assertThat(request.getHeaderNames(), empty());
    assertThat(request.getQueryParams().keySet(), empty());
  }

  @Test
  public void requestFromUriString() {
    HttpRequest request = builder.uri("http://localhost:8081/somePath/here").build();
    assertThat(request.getMethod(), is("GET"));
    assertThat(request.getPath(), is("/somePath/here"));
    assertThat(request.getEntity(), is(instanceOf(EmptyHttpEntity.class)));
    assertThat(request.getHeaderNames(), empty());
    assertThat(request.getQueryParams().keySet(), empty());
  }

  @Test
  public void syntheticRequest() {
    HttpRequest request = builder.uri("/somePath/here").build();
    assertThat(request.getMethod(), is("GET"));
    assertThat(request.getPath(), is("/somePath/here"));
    assertThat(request.getEntity(), is(instanceOf(EmptyHttpEntity.class)));
    assertThat(request.getHeaderNames(), empty());
    assertThat(request.getQueryParams().keySet(), empty());
  }

  @Test
  public void failWithoutUri() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage(containsString("URI must be specified"));
    builder.build();
  }

  @Test
  public void complexResponse() {
    MultiMap<String, String> paramMap = new MultiMap<>();

    paramMap.put(name, value);
    HttpRequest request = builder.entity(new ByteArrayHttpEntity("test".getBytes()))
        .uri(URI_VALUE)
        .method(POST)
        .queryParams(paramMap)
        .headers(paramMap)
        .addHeader(name.toUpperCase(), value.toUpperCase())
        .build();

    assertThat(request.getUri(), is(URI.create(URI_VALUE)));
    assertThat(request.getMethod(), is(POST.name()));
    assertThat(request.getEntity(), is(instanceOf(ByteArrayHttpEntity.class)));
    assertThat(request.getHeaderNames(), hasItems(name));
    assertThat(request.getHeaderValues(name), hasItems(value, value.toUpperCase()));
    MultiMap<String, String> requestQueryParams = request.getQueryParams();
    assertThat(requestQueryParams.keySet(), hasItems(name));
    assertThat(requestQueryParams.getAll(name), hasItems(value));
  }

  @Test
  public void headerManipulation() {
    builder.uri(URI_VALUE);
    assertThat(builder.build().getHeaderNames(), empty());

    String otherValue = "otherValue";
    MultiMap<String, String> multiMap = new MultiMap<>();
    multiMap.put(name, value);
    multiMap.put(name, otherValue);

    // add multiple valued header through parameter map and individually
    builder.headers(multiMap);
    builder.addHeader(name, value);
    assertThat(builder.getHeaderValues(name), hasItems(value, otherValue, value));
    assertThat(builder.build().getHeaderValues(name), hasItems(value, otherValue, value));

    // remove header
    builder.removeHeader(name);
    assertThat(builder.build().getHeaderNames(), empty());
  }

  @Test
  public void headerCheck() {
    HttpRequest request = builder.uri(URI_VALUE).addHeader(name, value).build();

    assertThat(request.containsHeader(name), is(true));
    assertThat(request.containsHeader("wat"), is(false));
  }
}
