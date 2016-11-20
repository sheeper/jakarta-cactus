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
package org.apache.cactus.sample.servlet.unit;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;

import java.io.IOException;

/**
 * Test the usage of the <code>out</code> implicit object when using
 * <code>JspTestCase</code>.
 *
 * @version $Id: TestJspOut.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestJspOut extends JspTestCase
{
    /**
     * Verify that we can write some text to the output Jsp writer.
     * 
     * @exception IOException on test failure
     */
    public void testOut() throws IOException
    {
        out.print("some text sent back using out");
    }

    /**
     * Verify that we can write some text to the output Jsp writer.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endOut(WebResponse theResponse) throws IOException
    {
        assertEquals("some text sent back using out",
            theResponse.getText());
    }
}
