/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.store;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.api.store.PartitionableExpirableObjectStore;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.component.InternalComponent;
import org.mule.runtime.core.api.config.i18n.CoreMessages;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.util.FileUtils;
import org.mule.runtime.core.api.util.UUID;
import org.mule.runtime.core.internal.util.store.PersistentObjectStorePartition;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

public class PartitionedPersistentObjectStore<T extends Serializable> extends AbstractPartitionableObjectStore<T>
    implements PartitionableExpirableObjectStore<T>, InternalComponent, MuleContextAware {

  private static final Logger LOGGER = getLogger(PartitionedPersistentObjectStore.class);
  public static final String OBJECT_STORE_DIR = "objectstore";

  protected MuleContext muleContext;
  private File storeDirectory;
  private Map<String, PersistentObjectStorePartition> partitionsByName = new HashMap<>();
  private boolean initialized = false;

  public PartitionedPersistentObjectStore() {
    super();
  }

  public PartitionedPersistentObjectStore(MuleContext context) {
    super();
    muleContext = context;
  }

  protected PartitionedPersistentObjectStore(Map<String, PersistentObjectStorePartition> getPartitionsByName) {
    this.partitionsByName = getPartitionsByName;
  }

  @Override
  public synchronized void open() throws ObjectStoreException {
    if (!initialized) {
      initObjectStoreDirectory();
      loadPreviousStoredPartitions();
      createDefaultPartition();
      initialized = true;
    }
  }

  private void createDefaultPartition() throws ObjectStoreException {
    if (!partitionsByName.containsKey(DEFAULT_PARTITION_NAME)) {
      createPartition(DEFAULT_PARTITION_NAME);
    }
  }

  @Override
  public synchronized void open(String partitionName) throws ObjectStoreException {
    open();
    if (!partitionsByName.containsKey(partitionName)) {
      createPartition(partitionName);
    }
  }

  @Override
  public void close(String partitionName) throws ObjectStoreException {}

  private void createPartition(String partitionName) throws ObjectStoreException {
    PersistentObjectStorePartition persistentObjectStorePartition =
        new PersistentObjectStorePartition(muleContext, partitionName, getNewPartitionDirectory(partitionName));
    persistentObjectStorePartition.open();
    partitionsByName.putIfAbsent(partitionName, persistentObjectStorePartition);
  }

  private File getNewPartitionDirectory(String partitionName) {
    return new File(storeDirectory, getPartitionDirectoryName(partitionName));
  }

  protected String getPartitionDirectoryName(String partitionName) {
    // By default an UUID is used.
    return UUID.getUUID();
  }

  @Override
  public boolean isPersistent() {
    return true;
  }

  @Override
  protected boolean doContains(String key, String partitionName) throws ObjectStoreException {
    return getPartitionObjectStore(partitionName).contains(key.toString());
  }

  @Override
  protected void doStore(String key, T value, String partitionName) throws ObjectStoreException {
    getPartitionObjectStore(partitionName).store(key.toString(), value);
  }

  @Override
  protected T doRetrieve(String key, String partitionName) throws ObjectStoreException {
    return getPartitionObjectStore(partitionName).retrieve(key.toString());
  }

  @Override
  protected T doRemove(String key, String partitionName) throws ObjectStoreException {
    return getPartitionObjectStore(partitionName).remove(key.toString());
  }

  @Override
  public List<String> allKeys(String partitionName) throws ObjectStoreException {
    return getPartitionObjectStore(partitionName).allKeys();
  }

  @Override
  public Map<String, T> retrieveAll(String partitionName) throws ObjectStoreException {
    return getPartitionObjectStore(partitionName).retrieveAll();
  }

  @Override
  public void clear(String partitionName) throws ObjectStoreException {
    getPartitionObjectStore(partitionName).clear();
  }

  protected PersistentObjectStorePartition<T> getPartitionObjectStore(String partitionName) throws ObjectStoreException {
    if (!partitionsByName.containsKey(partitionName)) {
      throw new ObjectStoreException(CoreMessages.createStaticMessage("No partition named: " + partitionName));
    }
    return partitionsByName.get(partitionName);
  }

  @Override
  public List<String> allPartitions() throws ObjectStoreException {
    File[] files = storeDirectory.listFiles();
    if (files == null) {
      return new ArrayList<>();
    }

    // sort the files so they are in the order in which their ids were generated
    // in store()
    Arrays.sort(files);
    List<String> partitions = new ArrayList<>();

    for (File file : files) {
      if (file.isDirectory()) {
        partitions.add(file.getName());
      }
    }
    return partitions;
  }

  private void initObjectStoreDirectory() {
    if (storeDirectory == null) {
      String path = getWorkingDirectory() + File.separator + OBJECT_STORE_DIR;
      storeDirectory = FileUtils.newFile(path);
      if (!storeDirectory.exists()) {
        createStoreDirectory(storeDirectory);
      }
    }
  }

  private synchronized void createStoreDirectory(File directory) {
    // To support concurrency we need to check if directory exists again inside
    // synchronized method
    if (!directory.exists() && !directory.mkdirs()) {
      I18nMessage message = CoreMessages.failedToCreate("object store directory " + directory.getAbsolutePath());
      throw new MuleRuntimeException(message);
    }
  }


  private void loadPreviousStoredPartitions() throws ObjectStoreException {
    File[] directories = storeDirectory.listFiles(File::isDirectory);
    if (directories == null) {
      return;
    }
    for (File partitionDirectory : directories) {
      try {
        PersistentObjectStorePartition persistentObjectStorePartition =
            new PersistentObjectStorePartition(muleContext, partitionDirectory);
        persistentObjectStorePartition.open();
        partitionsByName.putIfAbsent(persistentObjectStorePartition.getPartitionName(), persistentObjectStorePartition);
      } catch (Exception e) {
        LOGGER.error("Could not restore partition under directory " + partitionDirectory.getAbsolutePath());
      }
    }
  }

  @Override
  @Inject
  public void setMuleContext(MuleContext context) {
    muleContext = context;
  }

  @Override
  public void expire(long entryTTL, int maxEntries) throws ObjectStoreException {
    expire(entryTTL, maxEntries, DEFAULT_PARTITION_NAME);
  }

  @Override
  public void disposePartition(String partitionName) throws ObjectStoreException {
    this.getPartitionObjectStore(partitionName).close();
  }

  @Override
  public void expire(long entryTTL, int maxEntries, String partitionName) throws ObjectStoreException {
    getPartitionObjectStore(partitionName).expire(entryTTL, maxEntries);
  }

  protected String getWorkingDirectory() {
    return muleContext.getConfiguration().getWorkingDirectory();
  }

}
