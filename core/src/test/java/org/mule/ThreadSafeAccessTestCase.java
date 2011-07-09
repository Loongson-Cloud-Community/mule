/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule;

import org.mule.api.ThreadSafeAccess;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ThreadSafeAccessTestCase extends AbstractThreadSafeAccessTestCase
{
    @Test
    public void testConfig()
    {
        assertTrue(ThreadSafeAccess.AccessControl.isFailOnMessageScribbling());
        assertTrue(ThreadSafeAccess.AccessControl.isAssertMessageAccess());
    }

    @Test
    public void testMessage() throws InterruptedException
    {
        Map<String, Object> nullMap = null;
        basicPattern(new DefaultMuleMessage(new Object(), nullMap, muleContext));
        newCopy(new DefaultMuleMessage(new Object(), nullMap, muleContext));
        resetAccessControl(new DefaultMuleMessage(new Object(), nullMap, muleContext));
    }

    @Test
    public void testEvent() throws Exception
    {
        basicPattern(dummyEvent());
        newCopy(dummyEvent());
        resetAccessControl(dummyEvent());
    }
}
