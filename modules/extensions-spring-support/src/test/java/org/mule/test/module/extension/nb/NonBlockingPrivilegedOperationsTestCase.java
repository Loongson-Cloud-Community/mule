/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.nb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.module.extension.internal.loader.privileged.extension.PrivilegedNonBlockingComponentExecutor.OUTPUT;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;
import org.mule.runtime.module.extension.internal.loader.privileged.extension.PrivilegedExtension;

import org.junit.Test;

public class NonBlockingPrivilegedOperationsTestCase extends ExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "privileged-non-blocking-config.xml";
  }

  @Override
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return new Class<?>[] {PrivilegedExtension.class};
  }

  @Override
  protected void doSetUp() throws Exception {
    muleContext.getInjector().inject(this);
  }

  @Test
  public void privilegedNonBlockingOperation() throws Exception {
    String value = flowRunner("privilegedNonBlocking").run().getMessage().getPayload().getValue().toString();
    assertThat(value, equalTo(OUTPUT));
  }
}
