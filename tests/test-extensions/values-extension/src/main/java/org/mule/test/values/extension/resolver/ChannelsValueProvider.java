/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.values.extension.resolver;

import java.util.Set;

import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.sdk.api.values.Value;
import org.mule.sdk.api.values.ValueBuilder;
import org.mule.sdk.api.values.ValueProvider;
import org.mule.test.values.extension.connection.ChatConnection;

public class ChannelsValueProvider implements ValueProvider {

  @Connection
  private ChatConnection chatConnection;

  @Parameter
  private String workspace;

  @Override
  public Set<Value> resolve() {
    return ValueBuilder.getValuesFor(chatConnection.getChannels(workspace));
  }

  @Override
  public String getId() {
    return "Channels value provider";
  }
}
