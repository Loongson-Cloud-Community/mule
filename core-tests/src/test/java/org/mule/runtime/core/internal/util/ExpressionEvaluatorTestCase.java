/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.weave.v2.el.ByteArrayBasedCursorStreamProvider;

import org.junit.Test;

public class ExpressionEvaluatorTestCase extends AbstractMuleContextTestCase {

  @Test
  public void handleNullEvent() throws MuleException {
    TypedValue evaluate = muleContext.getExpressionManager().evaluate("%dw 2.0\noutput application/json\n---\n{a: 1}");
    ByteArrayBasedCursorStreamProvider value = (ByteArrayBasedCursorStreamProvider) evaluate.getValue();
    String expected = "{\n" +
        "  \"a\": 1\n" +
        "}";
    assertThat(IOUtils.toString(value), is(expected));
  }
}
