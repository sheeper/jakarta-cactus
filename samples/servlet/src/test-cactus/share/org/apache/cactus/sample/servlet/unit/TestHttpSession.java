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

import org.apache.cactus.HttpSessionCookie;
import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests that manipulates the HTTP Session.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestHttpSession extends ServletTestCase
{
    /**
     * Verify that it is possible to ask for no automatic session creation in
     * the <code>beginXXX()</code> method.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginNoAutomaticSessionCreation(WebRequest theRequest)
    {
        theRequest.setAutomaticSession(false);
    }

    /**
     * Verify that it is possible to ask for no automatic session creation in
     * the <code>beginXXX()</code> method.
     */
    public void testNoAutomaticSessionCreation()
    {
        assertNull("A valid session has been found when no session should "
            + "exist", session);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can get hold of the jsessionid cookie returned by the
     * server.
     */
    public void testVerifyJsessionid()
    {
        // By default, Cactus will create an HTTP session.
    }
    
    /**
     * Verify that we can get hold of the jsessionid cookie returned by the
     * server.
     * 
     * @param theResponse the response from the server side.
     */
    public void endVerifyJsessionid(WebResponse theResponse)
    {
        assertNotNull(theResponse.getCookieIgnoreCase("jsessionid"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that Cactus can provide us with a real HTTP session cookie.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginCreateSessionCookie(WebRequest theRequest)
    {
        HttpSessionCookie sessionCookie = theRequest.getSessionCookie();
        assertNotNull("Session cookie should not be null", sessionCookie);
        theRequest.addCookie(sessionCookie);
    }

    /**
     * Verify that Cactus can provide us with a real HTTP session cookie.
     */
    public void testCreateSessionCookie()
    {
        assertTrue("A session should have been created prior to "
            + "this request", !session.isNew());
    }

}
