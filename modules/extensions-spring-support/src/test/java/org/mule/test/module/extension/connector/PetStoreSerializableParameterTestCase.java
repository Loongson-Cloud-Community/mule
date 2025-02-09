/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.connector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;
import org.mule.tck.core.streaming.DummyByteBufferManager;
import org.mule.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class PetStoreSerializableParameterTestCase extends AbstractExtensionFunctionalTestCase {

  private static final String DONKEY = "donkey";

  @Override
  protected String getConfigFile() {
    return "petstore-serializable-parameter.xml";
  }

  @Test
  public void staticSerializableParameter() throws Exception {
    assertThat(flowRunner("staticSerializableParameter").run().getMessage().getPayload().getValue(), is(DONKEY));
  }

  @Test
  public void dynamicSerializableParameter() throws Exception {
    Message message = flowRunner("dynamicSerializableParameter").withVariable("animal", DONKEY).run().getMessage();
    assertThat(message.getPayload().getValue(), is(DONKEY));
  }

  @Test
  public void inputStreamParameter() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(DONKEY.getBytes());
    Message message = flowRunner("dynamicSerializableParameter").withVariable("animal", inputStream).run().getMessage();
    assertThat(message.getPayload().getValue(), is(DONKEY));
  }

  @Test
  public void cursorStreamProviderParameter() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(DONKEY.getBytes());
    CursorStreamProvider provider =
        new InMemoryCursorStreamProvider(inputStream, InMemoryCursorStreamConfig.getDefault(), new DummyByteBufferManager());
    Message message = flowRunner("dynamicSerializableParameter").withVariable("animal", provider).run().getMessage();
    assertThat(message.getPayload().getValue(), is(DONKEY));
  }
}
