/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.source;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.test.heisenberg.extension.HeisenbergSource;

import java.lang.reflect.InvocationTargetException;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SmallTest
public class DefaultSdkSourceFactoryTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void create() {
    assertThat(new DefaultSdkSourceFactory(HeisenbergSource.class).createMessageSource().getValue().get(),
               is(instanceOf(HeisenbergSource.class)));
  }

  @Test
  public void nullType() {
    expectedException.expect(IllegalArgumentException.class);
    new DefaultSdkSourceFactory(null);
  }

  @Test
  public void notInstantiable() {
    expectedException.expect(IllegalArgumentException.class);
    new DefaultSdkSourceFactory(Source.class);
  }

  @Test
  public void exceptionOnInstantiation() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(Matchers.instanceOf(InvocationTargetException.class));
    new DefaultSdkSourceFactory(UncreatableSource.class).createMessageSource();
  }

  public static class UncreatableSource extends Source {

    public UncreatableSource() {
      throw new IllegalArgumentException();
    }

    @Override
    public void onStart(SourceCallback sourceCallback) throws MuleException {

    }

    @Override
    public void onStop() {

    }
  }
}
