/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.soap.api.runtime.connection.transport;

import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.extension.api.soap.MessageDispatcherProvider;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.soap.api.message.dispatcher.DefaultHttpMessageDispatcher;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link MessageDispatcherProvider} sends a soap message over http using a default configuration.
 *
 * @since 4.0
 */
public class DefaultHttpMessageDispatcherProvider implements MessageDispatcherProvider<MessageDispatcher>, Lifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpMessageDispatcher.class);

  @Inject
  private HttpService httpService;

  private HttpClient httpClient;

  @Override
  public MessageDispatcher connect() throws ConnectionException {
    return new DefaultHttpMessageDispatcher(httpClient);
  }

  @Override
  public void disconnect(MessageDispatcher connection) {
    disposeIfNeeded(connection, LOGGER);
  }

  @Override
  public ConnectionValidationResult validate(MessageDispatcher connection) {
    return success();
  }


  @Override
  public void dispose() {
    // Do nothing
  }

  @Override
  public void initialise() throws InitialisationException {
    httpClient = httpService.getClientFactory().create(new HttpClientConfiguration.Builder()
        .setName("soap-extension")
        .build());
  }

  @Override
  public void stop() throws MuleException {
    httpClient.stop();
  }

  @Override
  public void start() throws MuleException {
    httpClient.start();
  }
}
