/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.processor;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.tck.size.SmallTest;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class AbstractMuleObjectOwnerTestCase {

  @Mock
  private TestClass mockObject1;

  @Mock
  private TestClass mockObject2;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private MuleContext mockMuleContext;

  @Mock
  private FlowConstruct mockFlowConstruct;

  private AbstractMuleObjectOwner<TestClass> abstractMuleObjectOwner;

  @Before
  public void before() {
    abstractMuleObjectOwner = new AbstractMuleObjectOwner<TestClass>() {

      @Override
      protected List<TestClass> getOwnedObjects() {
        return Arrays.asList(mockObject1, mockObject2);
      }
    };
    abstractMuleObjectOwner.setMuleContext(mockMuleContext);
  }

  @Test
  public void testInitialise() throws Exception {
    abstractMuleObjectOwner.initialise();
    verify(mockObject1).initialise();
    verify(mockObject2).initialise();
    // TODO TMULE-10764 Injection should only happen once
    verify(mockObject1, times(3)).setMuleContext(mockMuleContext);
    verify(mockObject2, times(3)).setMuleContext(mockMuleContext);
  }

  @Test
  public void testDispose() throws Exception {
    abstractMuleObjectOwner.dispose();
    verify(mockObject1).dispose();
    verify(mockObject2).dispose();
  }

  @Test
  public void testStart() throws Exception {
    abstractMuleObjectOwner.start();
    verify(mockObject1).start();
    verify(mockObject2).start();
  }

  @Test
  public void testStop() throws Exception {
    abstractMuleObjectOwner.stop();
    verify(mockObject1).stop();
    verify(mockObject2).stop();
  }

  public class TestClass implements Lifecycle, MuleContextAware {

    @Override
    public void dispose() {}

    @Override
    public void initialise() throws InitialisationException {}

    @Override
    public void setMuleContext(MuleContext context) {}

    @Override
    public void start() throws MuleException {}

    @Override
    public void stop() throws MuleException {}
  }
}
