/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.apache.cactus.internal.util;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Unit tests for the {@link IoUtil} class.
 *
 * @version $Id: TestIoUtil.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class TestIoUtil extends TestCase
{
    /**
     * Verify that the <code>getText()</code> method reads properly all bytes 
     * from an input stream.
     * 
     * @exception IOException on error
     */
    public void testGetTextOk() throws IOException
    {
        String expected = 
            "<html><head/>\n<body>A GET request</body>\n</html>\n";
        ByteArrayInputStream in = new ByteArrayInputStream(expected.getBytes());

        String result = IoUtil.getText(in);

        assertEquals(expected, result);
    }

    /**
     * Verify that the <code>getText()</code> method works when the input 
     * stream does not contain any data.
     * 
     * @exception IOException on error
     */
    public void testGetTextEmpty() throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());

        String result = IoUtil.getText(in);

        assertEquals("", result);
    }
}
