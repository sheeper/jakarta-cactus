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

import java.io.UnsupportedEncodingException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.ServletURL;

/**
 * Encapsulation class for the Servlet 2.3 API <code>HttpServletRequest</code>.
 * This is an implementation that delegates all the call to the
 * <code>HttpServletRequest</code> object passed in the constructor except for
 * some overiden methods which are use to simulate a URL. This is to be able to
 * simulate any URL that would have been used to call the test method : if this
 * was not done, the URL that would be returned (by calling the
 * <code>getRequestURI()</code> method or others alike) would be the URL of the
 * Cactus redirector servlet and not a URL that the test case want to simulate.
 *
 * @version $Id$
 */
public class HttpServletRequestWrapper 
    extends AbstractHttpServletRequestWrapper
{
    /**
     * Construct an <code>HttpServletRequest</code> instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the request
     * would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public HttpServletRequestWrapper(HttpServletRequest theRequest, 
        ServletURL theURL)
    {
        super(theRequest, theURL);
    }

    // Not modified methods --------------------------------------------------

    /**
     * @return the URL from the simulated URL or the real URL
     *         if a simulation URL has not been defined.
     * @see HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL()
    {
        StringBuffer result;

        if (this.url != null)
        {
            result = new StringBuffer(this.url.getProtocol() + "://"
                + getServerName() + ":" + getServerPort()
                + getRequestURI());
        }
        else
        {
            result = this.request.getRequestURL();
        }

        return result;
    }

    /**
     * @see HttpServletRequest#setCharacterEncoding(String)
     */
    public void setCharacterEncoding(String theEnvironment)
        throws UnsupportedEncodingException
    {
        this.request.setCharacterEncoding(theEnvironment);
    }

    /**
     * @see HttpServletRequest#getParameterMap()
     */
    public Map getParameterMap()
    {
        return this.request.getParameterMap();
    }
}
