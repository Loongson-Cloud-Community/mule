/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.runtime.core.internal.context.DefaultMuleContext;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.core.api.util.queue.DefaultQueueConfiguration;
import org.mule.tck.core.util.queue.AbstractTransactionQueueManagerTestCase;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class FilePersistenceTestCase extends AbstractTransactionQueueManagerTestCase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Override
  protected TransactionalQueueManager createQueueManager() throws Exception {
    TransactionalQueueManager mgr = new TransactionalQueueManager();
    MuleConfiguration mockConfiguration = mock(MuleConfiguration.class);
    when(mockConfiguration.getWorkingDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
    when(mockConfiguration.getMaxQueueTransactionFilesSizeInMegabytes()).thenReturn(100);
    ((DefaultMuleContext) muleContext).setMuleConfiguration(mockConfiguration);

    mgr.setMuleContext(muleContext);
    mgr.initialise();
    mgr.setDefaultQueueConfiguration(new DefaultQueueConfiguration(0, true));
    return mgr;
  }

  @Override
  protected boolean isPersistent() {
    return true;
  }
}
