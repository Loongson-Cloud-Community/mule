/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.simple;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SmallTest
public class StringToEnumTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  enum TestEnum {
    A, B
  }

  private StringToEnum transformer = new StringToEnum(TestEnum.class);

  @Test
  public void transform() throws Exception {
    for (TestEnum value : TestEnum.values()) {
      assertThat(transformer.transform(value.name()), is(value));
    }
  }

  @Test
  public void illegalValue() throws Exception {
    expectedException.expect(TransformerException.class);
    expectedException.expectCause(instanceOf(IllegalArgumentException.class));

    transformer.transform("NOT ENUM VALUE");
  }

  @Test
  public void nullClass() {
    expectedException.expect(IllegalArgumentException.class);
    new StringToEnum(null);
  }

  @Test
  public void name() {
    String name = format("StringTo%sTransformer", TestEnum.class.getName());
    assertThat(transformer.getName(), is(name));
  }

}
