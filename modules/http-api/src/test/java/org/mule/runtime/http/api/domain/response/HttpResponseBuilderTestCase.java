/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.domain.response;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mule.test.allure.AllureConstants.HttpFeature.HTTP_SERVICE;
import static org.mule.test.allure.AllureConstants.HttpFeature.HttpStory.RESPONSE_BUILDER;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.http.api.domain.message.response.HttpResponseBuilder;

import java.util.Collection;
import java.util.Optional;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(HTTP_SERVICE)
@Story(RESPONSE_BUILDER)
public class HttpResponseBuilderTestCase {

  private HttpResponseBuilder builder = HttpResponse.builder();
  private String header = "HEADER";
  private String value = "value";

  @Test
  public void defaultResponse() {
    HttpResponse response = builder.build();
    assertThat(response.getStatusCode(), is(200));
    assertThat(response.getReasonPhrase(), isEmptyString());
    assertThat(response.getEntity(), is(instanceOf(EmptyHttpEntity.class)));
    assertThat(response.getHeaderNames(), empty());
  }

  @Test
  public void complexResponse() {
    HttpResponse response = builder
        .entity(new ByteArrayHttpEntity("test".getBytes()))
        .statusCode(418)
        .reasonPhrase("I'm a teapot")
        .addHeader(header, value)
        .build();

    assertThat(response.getStatusCode(), is(418));
    assertThat(response.getReasonPhrase(), is("I'm a teapot"));
    assertThat(response.getEntity(), is(instanceOf(ByteArrayHttpEntity.class)));
    assertThat(response.getHeaderNames(), hasItems(equalToIgnoringCase(header)));
    assertThat(response.getHeaderValuesIgnoreCase(header), hasItems(value));
  }

  @Test
  public void headerManipulation() {
    assertThat(builder.build().getHeaderNames(), empty());

    // add initial header
    builder.addHeader(header, value);
    Optional<String> headerValue = builder.getHeaderValue(header);
    assertThat(headerValue.isPresent(), is(true));
    assertThat(headerValue.get(), is(value));
    Collection<String> headerValues = builder.getHeaderValues(header);
    assertThat(headerValues, hasItems(value));
    assertThat(builder.build().getHeaderValuesIgnoreCase(header), hasItems(value));

    // add same header with different case
    builder.addHeader(header.toLowerCase(), value.toUpperCase());
    Optional<String> headerValue2 = builder.getHeaderValue(header);
    assertThat(headerValue2.isPresent(), is(true));
    assertThat(headerValue2.get(), is(value));
    Collection<String> headerValues2 = builder.getHeaderValues(header.toLowerCase());
    assertThat(headerValues2, hasItems(value, value.toUpperCase()));
    assertThat(builder.build().getHeaderValuesIgnoreCase(header), hasItems(value, value.toUpperCase()));

    // remove header
    builder.removeHeader(header);
    assertThat(builder.build().getHeaderNames(), empty());

  }

  @Test
  public void headerCheck() {
    HttpResponse response = builder.addHeader(header, value).build();

    assertThat(response.containsHeader(header), is(true));
    assertThat(response.containsHeader("wat"), is(false));
  }

}
