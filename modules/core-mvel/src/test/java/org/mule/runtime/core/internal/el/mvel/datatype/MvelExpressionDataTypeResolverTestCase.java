/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.datatype;

import static java.util.Collections.EMPTY_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.mvel2.MVEL.compileExpression;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.tck.junit4.matcher.DataTypeMatcher.like;

import org.mule.mvel2.ParserContext;
import org.mule.mvel2.compiler.CompiledExpression;
import org.mule.runtime.core.internal.el.mvel.MVELExpressionLanguage;
import org.mule.runtime.core.privileged.event.PrivilegedEvent;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Test;

public class MvelExpressionDataTypeResolverTestCase extends AbstractMuleContextTestCase {

  public static final String EXPRESSION_VALUE = "bar";
  public static final String MEL_EXPRESSION = "someExpression";

  private MvelDataTypeResolver dataTypeResolver;

  @Override
  protected boolean mockExprExecutorService() {
    return true;
  }

  @Test
  public void returnsDefaultDataTypeForNonNullValue() throws Exception {
    CompiledExpression compiledExpression = compileMelExpression();

    dataTypeResolver = new MvelDataTypeResolver(EMPTY_LIST);

    assertThat(dataTypeResolver.resolve(EXPRESSION_VALUE, (PrivilegedEvent) testEvent(), compiledExpression),
               like(String.class, ANY, null));
  }

  @Test
  public void returnsDefaultDataTypeForNullValue() throws Exception {
    CompiledExpression compiledExpression = compileMelExpression();

    dataTypeResolver = new MvelDataTypeResolver();

    assertThat(dataTypeResolver.resolve(null, (PrivilegedEvent) testEvent(), compiledExpression), like(Object.class, ANY, null));
  }

  private CompiledExpression compileMelExpression() {
    MVELExpressionLanguage expressionLanguage = new MVELExpressionLanguage(muleContext);
    return (CompiledExpression) compileExpression(MEL_EXPRESSION, new ParserContext(expressionLanguage.getParserConfiguration()));
  }

}
