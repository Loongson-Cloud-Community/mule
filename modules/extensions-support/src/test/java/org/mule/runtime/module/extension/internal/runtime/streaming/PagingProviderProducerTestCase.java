/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.streaming;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.extension.api.property.ClassLoaderModelProperty;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import org.mule.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import org.mule.runtime.tracer.api.component.ComponentTracer;
import org.mule.runtime.tracer.api.span.info.InitialSpanInfo;
import org.mule.tck.size.SmallTest;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class PagingProviderProducerTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private final ExtensionConnectionSupplier extensionConnectionSupplier = mock(ExtensionConnectionSupplier.class);
  private final ExecutionContextAdapter executionContext = mock(ExecutionContextAdapter.class);
  private final PagingProvider<Object, String> delegate = mock(PagingProvider.class);
  private final ConfigurationInstance config = mock(ConfigurationInstance.class);

  private PagingProviderProducer<String> producer;

  private PagingProviderProducer<String> createProducer() {
    return new PagingProviderProducer<>(delegate, config, executionContext, extensionConnectionSupplier,
                                        mock(ComponentTracer.class));
  }

  @Before
  public void setUp() throws MuleException {
    ExtensionModel extensionModel = mock(ExtensionModel.class);
    when(executionContext.getExtensionModel()).thenReturn(extensionModel);
    ClassLoaderModelProperty property = new ClassLoaderModelProperty(getClass().getClassLoader());
    when(extensionModel.getModelProperty(ClassLoaderModelProperty.class)).thenReturn(of(property));

    producer = createProducer();
    initMocks(producer);

    ConnectionHandler handler = mock(ConnectionHandler.class);
    when(handler.getConnection()).thenReturn(new Object());
    when(extensionConnectionSupplier.getConnection(eq(executionContext), any())).thenReturn(handler);
  }

  @Test
  public void produce() throws Exception {
    List<String> page = asList("bleh");
    when(delegate.getPage(any())).thenReturn(page);
    assertThat(page, sameInstance(producer.produce()));
  }

  @Test
  public void produceWithDifferentConnections() throws Exception {
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);

    produce();
    produce();

    verify(connectionHandler, times(2)).getConnection();
    verify(connectionHandler, times(2)).release();
  }

  @Test
  public void produceWithStickyConnection() throws Exception {
    when(delegate.useStickyConnections()).thenReturn(true);
    producer = createProducer();

    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);

    produce();
    produce();

    verify(connectionHandler, times(1)).getConnection();
    verify(connectionHandler, never()).release();

    producer.close();
    verify(connectionHandler).release();
  }

  @Test
  public void totalAvailable() {
    final int total = 10;
    when(delegate.getTotalResults(any())).thenReturn(of(total));
    assertThat(total, is(producer.getSize()));
  }

  @Test
  public void closeQuietly() throws Exception {
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);

    producer.close();
    verify(delegate).close(any());
    verify(connectionHandler, times(1)).release();
  }

  @Test(expected = Exception.class)
  public void closeNoisely() throws Exception {
    doThrow(new DefaultMuleException(new Exception())).when(delegate).close(any());
    producer.close();
  }

  @Test
  public void connectionIsInvalidatedOnConnectionExceptionInProduce() throws Exception {
    producer = createProducer();
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);
    doThrow(new RuntimeException(new ConnectionException("Invalid Connection"))).when(delegate).getPage(any());

    try {
      producer.produce();
    } catch (Exception e) {
      assertThat(e.getCause(), instanceOf(ConnectionException.class));
      verify(delegate, times(1)).close(any());
      verify(connectionHandler, times(1)).invalidate();
    }
  }

  @Test
  public void connectionIsReleasedOnExceptionInProduce() throws Exception {
    producer = createProducer();
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);
    doThrow(new IllegalArgumentException("Invalid arguments")).when(delegate).getPage(any());

    try {
      producer.produce();
    } catch (Exception e) {
      assertThat(e, instanceOf(IllegalArgumentException.class));
      verify(delegate, times(1)).close(any());
      verify(connectionHandler, times(1)).release();
    }
  }

  @Test
  public void pagingProviderDelegateIsClosedQuietlyOnExceptionInProduceFirstPage() throws Exception {
    producer = createProducer();
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);
    doThrow(new IllegalArgumentException("Invalid arguments")).when(delegate).getPage(any());
    doThrow(new DefaultMuleException("Error while closing delegate")).when(delegate).close(any());

    try {
      producer.produce();
    } catch (Exception e) {
      assertThat(e, instanceOf(IllegalArgumentException.class));
      verify(delegate, times(1)).close(any());
      verify(connectionHandler, times(1)).release();
    }
  }

  @Test
  public void connectionIsClosedQuietlyInClose() throws Exception {
    producer = createProducer();
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    doThrow(new IllegalArgumentException("There was a problem releasing the connection")).when(connectionHandler).release();
    when(extensionConnectionSupplier.getConnection(any(), any())).thenReturn(connectionHandler);

    producer.close();
    verify(delegate, times(1)).close(any());
    verify(connectionHandler, times(1)).release();
  }
}
