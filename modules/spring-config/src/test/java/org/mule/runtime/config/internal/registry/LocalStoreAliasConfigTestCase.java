/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.registry;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertSame;
import static org.mule.runtime.api.store.ObjectStoreManager.BASE_IN_MEMORY_OBJECT_STORE_KEY;
import static org.mule.runtime.api.store.ObjectStoreManager.BASE_PERSISTENT_OBJECT_STORE_KEY;
import static org.mule.runtime.api.util.MuleSystemProperties.DISABLE_REGISTRY_BOOTSTRAP_OPTIONAL_ENTRIES_PROPERTY;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_LOCAL_STORE_IN_MEMORY;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_LOCAL_STORE_PERSISTENT;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_QUEUE_MANAGER;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_STORE_MANAGER;
import static org.mule.runtime.core.internal.context.DefaultMuleContext.LOCAL_OBJECT_STORE_MANAGER_KEY;
import static org.mule.runtime.core.internal.context.DefaultMuleContext.LOCAL_QUEUE_MANAGER_KEY;

import org.junit.Rule;
import org.mule.runtime.config.internal.SpringXmlConfigurationBuilder;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Test;
import org.mule.tck.junit4.rule.SystemProperty;

public class LocalStoreAliasConfigTestCase extends AbstractMuleContextTestCase {

  // TODO W-10736276 Remove this
  @Rule
  public SystemProperty systemProperty = new SystemProperty(DISABLE_REGISTRY_BOOTSTRAP_OPTIONAL_ENTRIES_PROPERTY, "false");

  @Override
  protected ConfigurationBuilder getBuilder() throws Exception {
    return new SpringXmlConfigurationBuilder(new String[0], emptyMap());
  }

  @Test
  public void inMemoryObjectStore() throws Exception {
    this.testSame(BASE_IN_MEMORY_OBJECT_STORE_KEY, OBJECT_LOCAL_STORE_IN_MEMORY);
  }

  @Test
  public void persistentObjectStore() throws Exception {
    this.testSame(BASE_PERSISTENT_OBJECT_STORE_KEY, OBJECT_LOCAL_STORE_PERSISTENT);
  }

  @Test
  public void queueManager() throws Exception {
    this.testSame(OBJECT_QUEUE_MANAGER, LOCAL_QUEUE_MANAGER_KEY);
    assertSame(muleContext.getQueueManager(),
               ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(LOCAL_QUEUE_MANAGER_KEY));
  }

  @Test
  public void objectStoreManager() throws Exception {
    this.testSame(OBJECT_STORE_MANAGER, LOCAL_OBJECT_STORE_MANAGER_KEY);
    assertSame(muleContext.getObjectStoreManager(),
               ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(LOCAL_OBJECT_STORE_MANAGER_KEY));
  }

  private void testSame(String key1, String key2) {
    Object obj1 = ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(key1);
    Object obj2 = ((MuleContextWithRegistry) muleContext).getRegistry().lookupObject(key2);
    assertSame(obj1, obj2);
  }
}
