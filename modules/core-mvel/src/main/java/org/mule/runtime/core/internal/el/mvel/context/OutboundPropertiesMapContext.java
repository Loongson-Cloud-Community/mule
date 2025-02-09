/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.context;

import static java.util.Collections.emptyMap;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.message.InternalMessage;
import org.mule.runtime.core.privileged.el.context.AbstractMapContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OutboundPropertiesMapContext extends AbstractMapContext<Serializable> {

  private CoreEvent event;
  private CoreEvent.Builder eventBuilder;

  // TODO MULE-10471 Immutable event used in MEL/Scripting should be shared for consistency
  public OutboundPropertiesMapContext(CoreEvent event, CoreEvent.Builder eventBuilder) {
    this.event = event;
    this.eventBuilder = eventBuilder;
  }

  @Override
  public Serializable doGet(String key) {
    return ((InternalMessage) event.getMessage()).getOutboundProperty(key);
  }

  @Override
  public void doPut(String key, Serializable value) {
    eventBuilder.message(InternalMessage.builder(event.getMessage()).addOutboundProperty(key, value).build());
    event = eventBuilder.build();
  }

  @Override
  public void doRemove(String key) {
    eventBuilder.message(InternalMessage.builder(event.getMessage()).removeOutboundProperty(key).build());
    event = eventBuilder.build();
  }

  @Override
  public Set<String> keySet() {
    return ((InternalMessage) event.getMessage()).getOutboundPropertyNames();
  }

  @Override
  public void clear() {
    eventBuilder.message(InternalMessage.builder(event.getMessage()).outboundProperties(emptyMap()).build());
    event = eventBuilder.build();
  }

  @Override
  public String toString() {
    Map<String, Object> map = new HashMap<>();
    for (String key : ((InternalMessage) event.getMessage()).getOutboundPropertyNames()) {
      Object value = ((InternalMessage) event.getMessage()).getOutboundProperty(key);
      map.put(key, value);
    }
    return map.toString();
  }
}
