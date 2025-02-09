/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.soap.api.client;

import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.soap.api.client.metadata.SoapMetadataResolver;
import org.mule.runtime.soap.api.message.SoapRequest;
import org.mule.runtime.soap.api.message.SoapResponse;

/**
 * Contract for clients that consumes SOAP Web Services, and returns the response.
 *
 * @since 4.0
 */
public interface SoapClient extends Startable, Stoppable {

  /**
   * Sends a {@link SoapRequest} blocking the current thread until a response is available or the request times out.
   *
   * @param request    a {@link SoapRequest} instance.
   * @param dispatcher a {@link MessageDispatcher} that will be used to dispatch the {@link SoapRequest}
   * @return a {@link SoapResponse} instance with the XML content and Headers if any.
   */
  default SoapResponse consume(SoapRequest request, MessageDispatcher dispatcher) {
    return consume(request);
  }

  /**
   * Sends a {@link SoapRequest} blocking the current thread until a response is available or the request times out.
   *
   * @param request a {@link SoapRequest} instance.
   * @return a {@link SoapResponse} instance with the XML content and Headers if any.
   */
  SoapResponse consume(SoapRequest request);

  /**
   * @return a {@link SoapMetadataResolver} that can resolve the INPUT and OUTPUT metadata for the different Web Service
   *         Operations.
   */
  SoapMetadataResolver getMetadataResolver();
}
