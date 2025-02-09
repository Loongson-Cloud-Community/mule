/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api;

import static org.mule.runtime.api.exception.MuleException.INFO_LOCATION_KEY;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.api.notification.EnrichedNotificationInfo.createInfo;
import static org.mule.runtime.core.internal.exception.MessagingException.PAYLOAD_INFO_KEY;
import static org.mule.tck.util.MuleContextUtils.eventBuilder;

import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.config.DefaultMuleConfiguration;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.internal.exception.MessagingException;
import org.mule.runtime.core.internal.exception.MessagingExceptionLocationProvider;
import org.mule.runtime.core.privileged.processor.AnnotatedProcessor;
import org.mule.tck.SerializationTestUtils;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.size.SmallTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.hamcrest.core.Is;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

@SmallTest
public class MessagingExceptionTestCase extends AbstractMuleContextTestCase {

  private static final String message = "a message";
  private static final String value = "Hello world!";

  private boolean originalVerboseExceptions;

  private final MessagingExceptionLocationProvider locationProvider = new MessagingExceptionLocationProvider();

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private MuleContext mockContext;

  private CoreEvent testEvent;

  @Mock
  private FlowConstruct flowConstruct;

  @Mock
  private TransformationService transformationService;

  @Mock
  private ComponentLocation mockComponentLocation;

  @Before
  public void before() throws MuleException {
    originalVerboseExceptions = MuleException.verboseExceptions;

    DefaultMuleConfiguration mockConfiguration = mock(DefaultMuleConfiguration.class);
    when(mockConfiguration.getId()).thenReturn("MessagingExceptionTestCase");
    when(mockContext.getConfiguration()).thenReturn(mockConfiguration);

    testEvent = eventBuilder(muleContext).message(of(TEST_PAYLOAD)).build();
  }

  @After
  public void after() {
    MuleException.verboseExceptions = originalVerboseExceptions;
  }

  @Test
  public void getCauseExceptionWithoutCause() {
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent);
    assertThat(exception.getRootCause(), is(exception));
  }

  @Test
  public void getCauseExceptionWithMuleCause() {
    DefaultMuleException causeException = new DefaultMuleException("");
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.getRootCause(), is(causeException));
  }

  @Test
  public void getCauseExceptionWithMuleCauseWithMuleCause() {
    DefaultMuleException causeCauseException = new DefaultMuleException("");
    DefaultMuleException causeException = new DefaultMuleException(causeCauseException);
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.getRootCause(), is(causeCauseException));
  }

  @Test
  public void getCauseExceptionWithNonMuleCause() {
    IOException causeException = new IOException("");
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.getRootCause(), is(causeException));
  }

  @Test
  public void getCauseExceptionWithNonMuleCauseWithNonMuleCause() {
    ConnectException causeCauseException = new ConnectException();
    IOException causeException = new IOException(causeCauseException);
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.getRootCause(), is(causeCauseException));
  }

  @Test
  public void causedByWithNullCause() {
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent);
    assertThat(exception.causedBy(MessagingException.class), Is.is(true));
    assertThat(exception.causedBy(Exception.class), Is.is(true));
    assertThat(exception.causedBy(DefaultMuleException.class), Is.is(false));
    assertThat(exception.causedBy(IOException.class), Is.is(false));
  }

  @Test
  public void causedByWithMuleCauseWithMuleCause() {
    DefaultMuleException causeCauseException = new DefaultMuleException("");
    DefaultMuleException causeException = new DefaultMuleException(causeCauseException);
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.causedBy(DefaultMuleException.class), is(true));
    assertThat(exception.causedBy(MessagingException.class), is(true));
  }

  @Test
  public void causedByWithNonMuleCause() {
    IOException causeException = new IOException("");
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.causedBy(IOException.class), is(true));
    assertThat(exception.causedBy(MessagingException.class), is(true));
    assertThat(exception.causedBy(Exception.class), is(true));
    assertThat(exception.causedBy(NullPointerException.class), is(false));
  }

  @Test
  public void causedByWithNonMuleCauseWithNonMuleCause() {
    ConnectException causeCauseException = new ConnectException();
    IOException causeException = new IOException(causeCauseException);
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.causedBy(NullPointerException.class), is(false));
    assertThat(exception.causedBy(SocketException.class), is(true));
    assertThat(exception.causedBy(IOException.class), is(true));
    assertThat(exception.causedBy(MessagingException.class), is(true));
  }

  @Test
  public void causedExactlyByWithNullCause() {
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent);
    assertThat(exception.causedExactlyBy(MessagingException.class), Is.is(true));
    assertThat(exception.causedExactlyBy(Exception.class), Is.is(false));
    assertThat(exception.causedExactlyBy(DefaultMuleException.class), Is.is(false));
    assertThat(exception.causedExactlyBy(IOException.class), Is.is(false));
  }

  @Test
  public void causedExactlyByWithMuleCauseWithMuleCause() {
    DefaultMuleException causeCauseException = new DefaultMuleException("");
    DefaultMuleException causeException = new DefaultMuleException(causeCauseException);
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.causedExactlyBy(DefaultMuleException.class), is(true));
    assertThat(exception.causedExactlyBy(MessagingException.class), is(true));
  }

  @Test
  public void causedExactlyByWithNonMuleCause() {
    IOException causeException = new IOException("");
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.causedExactlyBy(IOException.class), is(true));
    assertThat(exception.causedExactlyBy(MessagingException.class), is(true));
    assertThat(exception.causedExactlyBy(Exception.class), is(false));
    assertThat(exception.causedExactlyBy(NullPointerException.class), is(false));
  }

  @Test
  public void causedExactlyByWithNonMuleCauseWithNonMuleCause() {
    ConnectException causeCauseException = new ConnectException();
    IOException causeException = new IOException(causeCauseException);
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, causeException);
    assertThat(exception.causedExactlyBy(ConnectException.class), is(true));
    assertThat(exception.causedExactlyBy(SocketException.class), is(false));
    assertThat(exception.causedExactlyBy(IOException.class), is(true));
    assertThat(exception.causedExactlyBy(MessagingException.class), is(true));
  }

  @Test
  public void withFailingProcessor() {
    AnnotatedProcessor mockProcessor = mock(AnnotatedProcessor.class);
    when(mockProcessor.getRepresentation()).thenReturn("Mock@1 @ MessagingExceptionTestCase:unknown:-1");
    MessagingException exception = new MessagingException(createStaticMessage(""), testEvent, mockProcessor);
    locationProvider.putContextInfo(exception.getExceptionInfo(), createInfo(testEvent, exception, mockProcessor), mockProcessor);
    assertThat(exception.getInfo().get(INFO_LOCATION_KEY).toString(),
               is("Mock@1 @ MessagingExceptionTestCase:unknown:-1"));
  }

  @Test
  public void serializableMessagingException() throws Exception {
    TestSerializableMessageProcessor processor = new TestSerializableMessageProcessor();
    processor.setValue(value);

    MessagingException e = new MessagingException(createStaticMessage(message), testEvent, processor);

    e = SerializationTestUtils.testException(e, muleContext);

    assertThat(e.getMessage(), containsString(message));
    assertThat(e.getFailingComponent(), not(nullValue()));
    assertThat(e.getFailingComponent(), instanceOf(TestSerializableMessageProcessor.class));
    assertThat(((TestSerializableMessageProcessor) e.getFailingComponent()).getValue(), is(value));
  }

  @Test
  public void nonSerializableMessagingException() throws Exception {
    TestNotSerializableMessageProcessor processor = new TestNotSerializableMessageProcessor();

    MessagingException e = new MessagingException(createStaticMessage(message), testEvent, processor);

    e = SerializationTestUtils.testException(e, muleContext);

    assertThat(e.getMessage(), containsString(message));
    assertThat(e.getFailingComponent(), is(nullValue()));
  }

  @Test
  public void payloadInfoConsumable() throws Exception {
    MuleException.verboseExceptions = true;

    CoreEvent testEvent = mock(CoreEvent.class);
    when(testEvent.getError()).thenReturn(empty());
    final ByteArrayInputStream payload = new ByteArrayInputStream(new byte[] {});
    Message muleMessage = of(payload);
    when(testEvent.getMessage()).thenReturn(muleMessage);
    MessagingException e = new MessagingException(createStaticMessage(message), testEvent);

    assertThat((String) e.getInfo().get(PAYLOAD_INFO_KEY), containsString(ByteArrayInputStream.class.getName() + "@"));

    verify(transformationService, never()).transform(muleMessage, DataType.STRING);
  }

  @Test
  public void payloadInfoNonVerbose() throws Exception {
    MuleException.verboseExceptions = false;

    CoreEvent testEvent = mock(CoreEvent.class);
    Message muleMessage = spy(of(""));
    when(testEvent.getMessage()).thenReturn(muleMessage);
    when(testEvent.getError()).thenReturn(empty());
    MessagingException e = new MessagingException(createStaticMessage(message), testEvent);

    assertThat(e.getInfo().get(PAYLOAD_INFO_KEY), nullValue());

    verify(muleMessage, never()).getPayload();
    verify(transformationService, never()).transform(muleMessage, DataType.STRING);
  }

  private static final class FailAnswer implements Answer<String> {

    private final String failMessage;

    private FailAnswer(String failMessage) {
      this.failMessage = failMessage;
    }

    @Override
    public String answer(InvocationOnMock invocation) throws Throwable {
      fail(failMessage);
      return null;
    }
  }

}
