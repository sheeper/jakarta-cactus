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
package org.apache.cactus.server;

import java.io.IOException;
import java.io.Writer;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.ServletURL;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class FilterTestCaller extends AbstractWebTestCaller
{
    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public FilterTestCaller(FilterImplicitObjects theObjects)
    {
        super(theObjects);
    }

    /**
     * @see AbstractWebTestCaller#setTestCaseFields(TestCase)
     */
    protected void setTestCaseFields(TestCase theTestInstance)
        throws Exception
    {
        if (!(theTestInstance instanceof FilterTestCase))
        {
            return; 
        }

        FilterTestCase filterInstance = (FilterTestCase) theTestInstance;
        FilterImplicitObjects filterImplicitObjects = 
            (FilterImplicitObjects) this.webImplicitObjects;

        // Sets the request field of the test case class
        // ---------------------------------------------
        // Extract from the HTTP request the URL to simulate (if any)
        HttpServletRequest request = 
            filterImplicitObjects.getHttpServletRequest();

        ServletURL url = ServletURL.loadFromRequest(request);

        Field requestField = filterInstance.getClass().getField("request");

        requestField.set(filterInstance, 
            new HttpServletRequestWrapper(request, url));

        // Set the response field of the test case class
        // ---------------------------------------------
        Field responseField = filterInstance.getClass().getField("response");

        responseField.set(filterInstance, 
            filterImplicitObjects.getHttpServletResponse());

        // Set the config field of the test case class
        // -------------------------------------------
        Field configField = filterInstance.getClass().getField("config");

        configField.set(filterInstance, 
            new FilterConfigWrapper(filterImplicitObjects.getFilterConfig()));

        // Set the filter chain of the test case class
        // -------------------------------------------
        Field chainField = filterInstance.getClass().getField("filterChain");

        chainField.set(filterInstance, filterImplicitObjects.getFilterChain());
    }

    /**
     * @see AbstractWebTestCaller#getResponseWriter()
     */
    protected Writer getResponseWriter() throws IOException
    {
        return this.webImplicitObjects.getHttpServletResponse().getWriter();
    }
}
