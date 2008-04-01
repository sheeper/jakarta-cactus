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
package org.apache.cactus.server;

import java.io.UnsupportedEncodingException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.ServletURL;

/**
 * Extends {@link AbstractHttpServletRequestWrapper} by adding the new methods 
 * of the Servlet 2.3 API specifications.
 *
 * @see AbstractHttpServletRequestWrapper
 * @version $Id: AbstractHttpServletRequestWrapper23.java 238993 2004-05-22 16:39:34Z vmassol $
 */
public abstract class AbstractHttpServletRequestWrapper23 
    extends AbstractHttpServletRequestWrapper
{
    /**
     * Construct a {@link HttpServletRequest} instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the 
     * request would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public AbstractHttpServletRequestWrapper23(HttpServletRequest theRequest, 
        ServletURL theURL)
    {
        super(theRequest, theURL);
    }

    // Unmodified methods --------------------------------------------------

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
     * {@inheritDoc}
     * @see HttpServletRequest#setCharacterEncoding(String)
     */
    public void setCharacterEncoding(String theEnvironment)
        throws UnsupportedEncodingException
    {
        this.request.setCharacterEncoding(theEnvironment);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getParameterMap()
     */
    public Map getParameterMap()
    {
        return this.request.getParameterMap();
    }
}
