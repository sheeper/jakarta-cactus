/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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