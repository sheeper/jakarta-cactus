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
package org.apache.maven.cactus.sample;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Tests of the <code>SampleServlet</code> Servlet class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestSampleServlet extends ServletTestCase
{
    /**
     * Verify that <code>isAuthenticated</code> works when the user is
     * authenticated.
     */
    public void testIsAuthenticatedAuthenticated()
    {
        SampleServlet servlet = new SampleServlet();

        session.setAttribute("authenticated", "true");
        
        assertTrue(servlet.isAuthenticated(request));
    }

    /**
     * Verify that <code>isAuthenticated</code> works when the user is
     * not authenticated.
     */
    public void testIsAuthenticatedNotAuthenticated()
    {
        SampleServlet servlet = new SampleServlet();

        assertTrue(!servlet.isAuthenticated(request));
    }

    /**
     * Verify that <code>isAuthenticated</code> works when there is no
     * HTTP Session.
     * 
     * @param theRequest the Cactus request object
     */
    public void beginIsAuthenticatedNoSession(WebRequest theRequest)
    {
        theRequest.setAutomaticSession(false);
    }
    
    /**
     * Verify that <code>isAuthenticated</code> works when there is no
     * HTTP Session.
     */
    public void testIsAuthenticatedNoSession()
    {
        SampleServlet servlet = new SampleServlet();

        assertTrue(!servlet.isAuthenticated(request));
    }
}
