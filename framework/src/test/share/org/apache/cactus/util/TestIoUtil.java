/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.cactus.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.cactus.internal.util.IoUtil;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link IoUtil} class.
 *
 * @version $Id$
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
