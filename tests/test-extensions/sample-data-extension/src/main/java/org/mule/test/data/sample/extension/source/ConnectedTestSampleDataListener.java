/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.data.sample.extension.source;

import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.sdk.api.annotation.data.sample.SampleData;
import org.mule.test.data.sample.extension.SampleDataConnection;
import org.mule.test.data.sample.extension.provider.ConnectedTestSampleDataProvider;

@Alias("connected-listener")
@SampleData(ConnectedTestSampleDataProvider.class)
@MediaType(TEXT_PLAIN)
public class ConnectedTestSampleDataListener extends SimpleTestSampleDataListener {

  @Connection
  protected ConnectionProvider<SampleDataConnection> connection;

}
