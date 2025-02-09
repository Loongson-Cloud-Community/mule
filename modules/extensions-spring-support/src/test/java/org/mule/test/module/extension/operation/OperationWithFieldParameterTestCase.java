/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.operation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.mule.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

public class OperationWithFieldParameterTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "operation-field-parameter.xml";
  }

  @Test
  public void useFieldParameter() throws Exception {
    String value = flowRunner("spreadTheWord").run().getMessage().getPayload().getValue().toString();
    assertThat(value, equalTo("meat is badmeat is badmeat is badmeat is badmeat is bad"));
  }

  @Test
  public void fieldParameterAvailableAtInitialise() throws Exception {
    String value = flowRunner("negativeEloquence").run().getMessage().getPayload().getValue().toString();
    assertThat(value, equalTo("meat is bad"));
  }

  @Test
  public void fieldParameterWithDefaultValue() throws Exception {
    String value = flowRunner("defaultEloquence").run().getMessage().getPayload().getValue().toString();
    assertThat(value, equalTo("meat is badmeat is bad"));
  }
}
