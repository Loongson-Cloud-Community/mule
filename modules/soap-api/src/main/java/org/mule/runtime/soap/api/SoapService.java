/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.soap.api;

import org.mule.runtime.api.service.Service;
import org.mule.runtime.soap.api.client.SoapClient;
import org.mule.runtime.soap.api.client.SoapClientFactory;
import org.mule.api.annotation.NoImplement;

/**
 * Contract for a service that provides a Soap client factory.
 *
 * @since 4.0
 */
@NoImplement
public interface SoapService extends Service {

  /**
   * @return a {@link SoapClientFactory} instance capable of creating {@link SoapClient} instances.
   */
  SoapClientFactory getClientFactory();
}
