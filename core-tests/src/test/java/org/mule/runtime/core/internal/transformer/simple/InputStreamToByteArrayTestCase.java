/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import org.mule.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;
import org.mule.runtime.core.internal.streaming.bytes.SimpleByteBufferManager;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

@SmallTest
public class InputStreamToByteArrayTestCase extends AbstractMuleTestCase {

  private static final String DONKEY = "donkey";

  private InputStreamToByteArray transformer = new InputStreamToByteArray();

  @Test
  public void transformInputStream() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(DONKEY.getBytes());
    assertThat(transformer.transform(inputStream), equalTo(DONKEY.getBytes()));
  }

  @Test
  public void transformCursorStreamProvider() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(DONKEY.getBytes());
    CursorStreamProvider provider =
        new InMemoryCursorStreamProvider(inputStream, InMemoryCursorStreamConfig.getDefault(), new SimpleByteBufferManager());
    assertThat(transformer.transform(provider), equalTo(DONKEY.getBytes()));

  }
}

