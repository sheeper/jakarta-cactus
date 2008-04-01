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
package org.apache.cactus.internal.server;

import java.io.IOException;
import java.io.Writer;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.ServletURL;
import org.apache.cactus.server.AbstractPageContextWrapper;

/**
 * Call the test method on the server side after assigning the JSP implicit
 * objects using reflection.
 *
 * @version $Id: JspTestCaller.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class JspTestCaller extends ServletTestCaller
{
    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public JspTestCaller(JspImplicitObjects theObjects)
    {
        super(theObjects);
    }

    /**
     * {@inheritDoc}
     * @see AbstractWebTestCaller#setTestCaseFields(TestCase)
     */
    protected void setTestCaseFields(TestCase theTestInstance)
        throws Exception
    {
        if (!(theTestInstance instanceof JspTestCase))
        {
            return; 
        }

        JspTestCase jspInstance = (JspTestCase) theTestInstance;
        JspImplicitObjects jspImplicitObjects = 
            (JspImplicitObjects) this.webImplicitObjects;

        // Sets the Servlet-related implicit objects
        // -----------------------------------------
        super.setTestCaseFields(jspInstance);

        // Set the page context field of the test case class
        // -------------------------------------------------
        // Extract from the HTTP request the URL to simulate (if any)
        HttpServletRequest request = jspImplicitObjects.getHttpServletRequest();

        ServletURL url = ServletURL.loadFromRequest(request);

        Field pageContextField = jspInstance.getClass().getField("pageContext");

        pageContextField.set(jspInstance, 
            AbstractPageContextWrapper.newInstance(
                jspImplicitObjects.getPageContext(), url));

        // Set the JSP writer field of the test case class
        // -----------------------------------------------
        Field outField = jspInstance.getClass().getField("out");

        outField.set(jspInstance, jspImplicitObjects.getJspWriter());
    }

    /**
     * {@inheritDoc}
     * @see AbstractWebTestCaller#getResponseWriter()
     */
    protected Writer getResponseWriter() throws IOException
    {
        JspImplicitObjects jspImplicitObjects = 
            (JspImplicitObjects) this.webImplicitObjects;

        return jspImplicitObjects.getJspWriter();
    }
}
