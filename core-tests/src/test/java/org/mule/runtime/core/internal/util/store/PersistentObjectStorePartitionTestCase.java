/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.store;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mule.runtime.core.api.util.FileUtils.openDirectory;
import static org.mule.tck.SerializationTestUtils.addJavaSerializerToMockMuleContext;
import static org.mule.tck.util.MuleContextUtils.mockMuleContext;

import org.mule.runtime.api.store.ObjectDoesNotExistException;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class PersistentObjectStorePartitionTestCase extends AbstractMuleTestCase {

  @Rule
  public TemporaryFolder objectStoreFolder = new TemporaryFolder();

  private MuleContext muleContext = mockMuleContext();

  @Mock
  private MuleConfiguration muleConfiguration;

  private File workingDirectory;

  private PersistentObjectStorePartition partition;

  @Before
  public void setUp() throws Exception {
    when(muleContext.getExecutionClassLoader()).thenReturn(getClass().getClassLoader());
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);
    workingDirectory = objectStoreFolder.getRoot().getParentFile();
    when(muleConfiguration.getWorkingDirectory()).thenReturn(workingDirectory.getPath());
    addJavaSerializerToMockMuleContext(muleContext);
    partition = new PersistentObjectStorePartition(muleContext, "test", objectStoreFolder.getRoot());
    partition.open();
  }

  @Test
  public void indicatesUnexistentKeyOnRetrieveError() throws ObjectStoreException {
    final String nonExistentKey = "nonExistentKey";

    try {
      partition.retrieve(nonExistentKey);
      fail("Supposed to thrown an exception as key is not valid");
    } catch (ObjectDoesNotExistException e) {
      assertTrue(e.getMessage().contains(nonExistentKey));
    }
  }

  @Test
  public void skipAndMoveCorruptedOrUnreadableFiles() {
    final String KEY = "key";
    final String VALUE = "value";
    try {
      File.createTempFile("temp", ".obj", objectStoreFolder.getRoot());
      File corruptedFolder = openDirectory(workingDirectory.getAbsolutePath()
          + File.separator + PersistentObjectStorePartition.CORRUPTED_FOLDER);
      int corruptedBefore = corruptedFolder.list().length;

      partition.store(KEY, VALUE);
      // Expect the new stored object, and the partition-descriptor file
      assertEquals(2, objectStoreFolder.getRoot().listFiles().length);

      // Expect to have one more corrupted file in the corrupted folder
      assertEquals(corruptedBefore + 1, corruptedFolder.list().length);
    } catch (Exception e) {
      fail("Supposed to have skipped corrupted or unreadable files");
    }
  }

  @Test
  public void skipAndMoveCorruptedOrUnreadableFilesWithoutCreatingDir() throws Exception {
    final String KEY = "key";
    final String VALUE = "value";
    File corruptedFile = File.createTempFile("temp", ".obj", objectStoreFolder.getRoot());
    File corruptedFolder = openDirectory(workingDirectory.getAbsolutePath()
        + File.separator + PersistentObjectStorePartition.CORRUPTED_FOLDER);
    deleteDirectory(corruptedFolder);

    partition.store(KEY, VALUE);
    // Expect the new stored object, and the partition-descriptor file
    assertThat(objectStoreFolder.getRoot().listFiles().length, is(2));
    // Expect to have one more corrupted file in the corrupted folder
    assertThat(corruptedFolder.list().length, is(1));
    File directory = new File(corruptedFolder.getAbsolutePath() + "/" + corruptedFolder.list()[0]);
    assertThat(directory.isDirectory(), is(true));
    assertThat(directory.listFiles().length, is(1));
    assertThat(directory.listFiles()[0].getName(), is(corruptedFile.getName()));
  }

  @Test
  public void clear() throws Exception {
    final String KEY = "key";
    final String VALUE = "value";


    partition.store(KEY, VALUE);
    assertThat(partition.contains(KEY), is(true));
    assertThat(VALUE, is(partition.retrieve(KEY)));

    partition.clear();
    assertThat(partition.contains(KEY), is(false));
    assertThat("Partition descriptor doesn't exists", new File(objectStoreFolder.getRoot(), "partition-descriptor").exists(),
               is(true));
  }

  @Test
  public void clearBeforeLoading() throws Exception {
    partition.clear();
    assertEquals(0, partition.allKeys().size());
  }
}
