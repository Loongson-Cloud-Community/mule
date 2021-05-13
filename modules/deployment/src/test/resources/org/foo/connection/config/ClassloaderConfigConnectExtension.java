/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.foo.connection.config;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.io.InputStream;

import org.foo.connection.config.ClassloaderConfigConnectionProvider;

/**
 * Extension for testing purposes
 */
@Extension(name = "ClassloaderConfigConnect")
@Operations({ClassloaderConfigOperation.class})
@ConnectionProviders(ClassloaderConfigConnectionProvider.class)
public class ClassloaderConfigConnectExtension {

  private String fileMessage;

  public String getFileMessage() {
    return fileMessage;
  }
}