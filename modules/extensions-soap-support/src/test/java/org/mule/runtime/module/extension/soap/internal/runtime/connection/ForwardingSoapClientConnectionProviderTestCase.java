/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.soap.internal.runtime.connection;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.Injector;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.soap.MessageDispatcherProvider;
import org.mule.runtime.extension.api.soap.SoapServiceProvider;
import org.mule.runtime.extension.api.soap.SoapServiceProviderConfigurationException;
import org.mule.runtime.extension.api.soap.WebServiceDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ForwardingSoapClientConnectionProviderTestCase {

  private static final String ERROR_NAME = "AN_ERROR";
  private static final String ERROR_NS = "NS";
  private static final String ERROR_MESSAGE = "ERROR MESSAGE";
  private static final Exception EXCEPTION = new RuntimeException(ERROR_MESSAGE);
  private static final ErrorType ERROR_TYPE = mock(ErrorType.class);

  public ExpectedException expectedException = ExpectedException.none();

  private MessageDispatcherProvider dispatcherProvider = mock(MessageDispatcherProvider.class);
  private MuleContext ctx = mock(MuleContext.class);

  @Before
  public void setup() {
    when(ERROR_TYPE.getNamespace()).thenReturn(ERROR_NS);
    when(ERROR_TYPE.getIdentifier()).thenReturn(ERROR_NAME);
  }

  @Test
  public void invalidProvider() throws Exception {
    ConnectionValidationResult result =
        new ForwardingSoapClientConnectionProvider(new ValidableServiceProvider(false), dispatcherProvider, ctx).validate(null);
    assertThat(result.isValid(), is(false));
    assertThat(result.getException(), instanceOf(SoapServiceProviderConfigurationException.class));
    assertThat(result.getMessage(), is(ERROR_MESSAGE));
  }

  private class ValidableServiceProvider implements SoapServiceProvider {

    private final boolean valid;

    ValidableServiceProvider(boolean valid) {
      this.valid = valid;
    }

    @Override
    public List<WebServiceDefinition> getWebServiceDefinitions() {
      try {
        return asList(WebServiceDefinition.builder()
            .withId("dos")
            .withFriendlyName("Another Service Name")
            .withWsdlUrl(new URL("http://localhost.com/dos"))
            .withService("Service")
            .withPort("Port2").build());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void validateConfiguration() {
      if (!valid) {
        throw new SoapServiceProviderConfigurationException(ERROR_MESSAGE, EXCEPTION);
      }
    }
  }
}

