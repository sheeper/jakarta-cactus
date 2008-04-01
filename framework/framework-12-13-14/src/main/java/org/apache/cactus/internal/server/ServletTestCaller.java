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
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.ServletURL;
import org.apache.cactus.server.AbstractHttpServletRequestWrapper;
import org.apache.cactus.server.AbstractServletConfigWrapper;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method.
 *
 * @version $Id: ServletTestCaller.java 292559 2005-09-29 21:36:43Z kenney $
 */
public class ServletTestCaller extends AbstractWebTestCaller
{
    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public ServletTestCaller(ServletImplicitObjects theObjects)
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
        if (!(theTestInstance instanceof ServletTestCase))
        {
            return; 
        }
        
        ServletTestCase servletInstance = (ServletTestCase) theTestInstance;
        ServletImplicitObjects servletImplicitObjects = 
            (ServletImplicitObjects) this.webImplicitObjects;

        // Sets the request field of the test case class
        // ---------------------------------------------
        // Extract from the HTTP request the URL to simulate (if any)
        HttpServletRequest request = 
            servletImplicitObjects.getHttpServletRequest();

        ServletURL url = ServletURL.loadFromRequest(request);

        Field requestField = servletInstance.getClass().getField("request");

        requestField.set(servletInstance, 
            AbstractHttpServletRequestWrapper.newInstance(request, url));

        // Set the response field of the test case class
        // ---------------------------------------------
        Field responseField = servletInstance.getClass().getField("response");

        responseField.set(servletInstance, 
            servletImplicitObjects.getHttpServletResponse());

        // Set the config field of the test case class
        // -------------------------------------------
        Field configField = servletInstance.getClass().getField("config");

        configField.set(servletInstance, AbstractServletConfigWrapper.
            newInstance(servletImplicitObjects.getServletConfig()));

        // Set the session field of the test case class
        // --------------------------------------------
        // Create a Session object if the auto session flag is on
        if (isAutoSession())
        {
            HttpSession session = servletImplicitObjects.getHttpServletRequest()
                .getSession(true);

            Field sessionField = servletInstance.getClass().getField("session");

            sessionField.set(servletInstance, session);
        }
    }

    /**
     * {@inheritDoc}
     * @see AbstractWebTestCaller#getResponseWriter()
     */
    protected Writer getResponseWriter() throws IOException
    {
        return this.webImplicitObjects.getHttpServletResponse().getWriter();
    }
}
