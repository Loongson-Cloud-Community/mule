/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.http.api.client.proxy;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.test.allure.AllureConstants.HttpFeature.HTTP_SERVICE;
import static org.mule.test.allure.AllureConstants.HttpFeature.HttpStory.PROXY_CONFIG_BUILDER;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_SERVICE)
@Story(PROXY_CONFIG_BUILDER)
public class ProxyConfigBuilderTest extends AbstractProxyConfigTestCase<ProxyConfigBuilder> {

  private ProxyConfigBuilder proxyConfigBuilder = ProxyConfig.builder();

  @Override
  protected ProxyConfigBuilder getProxyConfigBuilder() {
    return proxyConfigBuilder;
  }

  @Test
  public void minimalConfig() {
    ProxyConfig config = proxyConfigBuilder.host(HOST).port(PORT).build();
    assertThat(config.getHost(), is(HOST));
    assertThat(config.getPort(), is(PORT));
    assertThat(config.getPassword(), nullValue());
    assertThat(config.getUsername(), nullValue());
  }

  @Test
  public void fullConfig() {
    ProxyConfig config =
        proxyConfigBuilder.host(HOST).port(PORT).username(USERNAME).password(PASSWORD).nonProxyHosts(NON_PROXY_HOSTS).build();
    assertThat(config.getHost(), is(HOST));
    assertThat(config.getPort(), is(PORT));
    assertThat(config.getPassword(), is(PASSWORD));
    assertThat(config.getUsername(), is(USERNAME));
    assertThat(config.getNonProxyHosts(), is(NON_PROXY_HOSTS));
  }

}
