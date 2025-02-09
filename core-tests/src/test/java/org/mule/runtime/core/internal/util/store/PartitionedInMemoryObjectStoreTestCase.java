/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.store;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.core.internal.store.PartitionedInMemoryObjectStore;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class PartitionedInMemoryObjectStoreTestCase extends AbstractMuleTestCase {

  private static final String TEST_PARTITION = "testPartition";
  private static final String TEST_VALUE = "testValue";
  private static final String TEST_KEY1 = "testKey1";
  private static final String TEST_KEY2 = "testKey2";
  private static final String TEST_KEY3 = "testKey3";

  private PartitionedInMemoryObjectStore<String> store;

  private long currentNanoTime = MILLISECONDS.toNanos(1);

  @Before
  public void setup() {
    store = new PartitionedInMemoryObjectStore() {

      @Override
      protected long getCurrentNanoTime() {
        return currentNanoTime;
      }
    };
  }

  @Test
  public void expireByTtlWithNegativeMaxEntriesAndEmptyStore() throws ObjectStoreException {
    store.expire(1, -1, TEST_PARTITION);
  }

  @Test
  public void expireByTtlMultipleKeysInsertedInTheSameNanoSecond() throws ObjectStoreException {
    store.store(TEST_KEY1, TEST_VALUE, TEST_PARTITION);
    store.store(TEST_KEY2, TEST_VALUE, TEST_PARTITION);

    currentNanoTime = MILLISECONDS.toNanos(2);

    store.store(TEST_KEY3, TEST_VALUE, TEST_PARTITION);
    store.expire(1, 100, TEST_PARTITION);

    assertThat(store.contains(TEST_KEY1, TEST_PARTITION), is(false));
    assertThat(store.contains(TEST_KEY2, TEST_PARTITION), is(false));
    assertThat(store.retrieve(TEST_KEY3, TEST_PARTITION), equalTo(TEST_VALUE));
  }

  @Test
  public void expireByNumberOfEntriesMultipleKeysInsertedInTheSameNanoSecond() throws ObjectStoreException {
    store.store(TEST_KEY1, TEST_VALUE, TEST_PARTITION);
    store.store(TEST_KEY2, TEST_VALUE, TEST_PARTITION);

    currentNanoTime = MILLISECONDS.toNanos(2);

    store.store(TEST_KEY3, TEST_VALUE, TEST_PARTITION);
    store.expire(10, 1, TEST_PARTITION);

    assertThat(store.contains(TEST_KEY1, TEST_PARTITION), is(false));
    assertThat(store.contains(TEST_KEY2, TEST_PARTITION), is(false));
    assertThat(store.retrieve(TEST_KEY3, TEST_PARTITION), equalTo(TEST_VALUE));
  }

  @Test
  public void removeKeyInsertedInTheSameNanosecondThanOther() throws ObjectStoreException {
    store.store(TEST_KEY1, TEST_VALUE, TEST_PARTITION);
    store.store(TEST_KEY2, TEST_VALUE, TEST_PARTITION);

    currentNanoTime = MILLISECONDS.toNanos(2);

    store.store(TEST_KEY3, TEST_VALUE, TEST_PARTITION);

    store.remove(TEST_KEY2, TEST_PARTITION);

    assertThat(store.retrieve(TEST_KEY1, TEST_PARTITION), equalTo(TEST_VALUE));
    assertThat(store.contains(TEST_KEY2, TEST_PARTITION), is(false));
    assertThat(store.retrieve(TEST_KEY3, TEST_PARTITION), equalTo(TEST_VALUE));
  }

  @Test
  public void removesDataOnClear() throws ObjectStoreException {
    PartitionedInMemoryObjectStore os = spy(store);

    os.store(TEST_KEY1, TEST_VALUE, TEST_PARTITION);
    assertThat(os.contains(TEST_KEY1, TEST_PARTITION), is(true));

    os.clear(TEST_PARTITION);
    assertThat(os.retrieveAll(TEST_PARTITION).size(), is(0));
  }

  @Test
  public void removesDataOnClose() throws ObjectStoreException {
    PartitionedInMemoryObjectStore os = spy(store);

    os.store(TEST_KEY1, TEST_VALUE, TEST_PARTITION);
    assertThat(os.contains(TEST_KEY1, TEST_PARTITION), is(true));

    os.close(TEST_PARTITION);
    assertThat(store.allPartitions(), is(empty()));
    verify(os, times(1)).disposePartition(TEST_PARTITION);
  }

}
