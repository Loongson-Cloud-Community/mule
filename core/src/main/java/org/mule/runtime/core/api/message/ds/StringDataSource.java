/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.message.ds;

import org.mule.runtime.api.metadata.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public final class StringDataSource implements DataSource {

  protected String content;
  protected MediaType contentType = MediaType.TEXT;
  protected String name = "StringDataSource";

  public StringDataSource(String payload) {
    super();
    content = payload;
  }

  public StringDataSource(String payload, String name) {
    super();
    content = payload;
    this.name = name;
  }

  public StringDataSource(String content, String name, MediaType contentType) {
    this.content = content;
    this.contentType = contentType;
    this.name = name;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(content.getBytes());
  }

  @Override
  public OutputStream getOutputStream() {
    throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
  }

  @Override
  public String getContentType() {
    return contentType.toString();
  }

  @Override
  public String getName() {
    return name;
  }
}

