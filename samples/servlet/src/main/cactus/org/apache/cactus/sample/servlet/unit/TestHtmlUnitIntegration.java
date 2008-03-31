/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.sample.servlet.unit;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.cactus.ServletTestCase;

/**
 * Test the HtmlpUnit integration.
 *
 * @version $Id$
 */
public class TestHtmlUnitIntegration extends ServletTestCase
{
    /**
     * Verify that the HtmlUnit integration works.
     * 
     * @exception IOException on test failure
     */
    public void testHtmlUnitGetText() throws IOException
    {
        PrintWriter pw = response.getWriter();

        pw.print("something to return for the test");
    }

    /**
     * Verify that HttpUnit integration works
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endHtmlUnitGetText(
       com.gargoylesoftware.htmlunit.WebResponse theResponse) throws IOException
    {
        String text = theResponse.getContentAsString();

        assertEquals("something to return for the test", text);
    }

    //-------------------------------------------------------------------------
// TODO: I have never use HtmlUnit and I don't find the real equivalent for 
//    this test. The next method failed.
//    /**
//     * Verify that we can set several headers in the response and
//     * assert them in endXXX().
//     */
//    public void testResponseAddHeadersHtmlUnit()
//    {
//        response.addHeader("X-Access-Header1", "value1");
//        response.addHeader("X-Access-Header2", "value2");
//    }
//
//    /**
//     * Verify that we can set several headers in the response and
//     * assert them in endXXX().
//     *
//     * @param theResponse the response from the server side.
//     */
//    public void endResponseAddHeadersHtmlUnit(
//    com.gargoylesoftware.htmlunit.WebResponse theResponse)
//    {
//        String value1 = 
//            theResponse.getResponseHeaderValue("X-Access-Header1");
//        String value2 =
//            theResponse.getResponseHeaderValue("X-Access-Header2");
//
//        assertEquals("value1", value1);
//        assertEquals("value2", value2);
//    }

}