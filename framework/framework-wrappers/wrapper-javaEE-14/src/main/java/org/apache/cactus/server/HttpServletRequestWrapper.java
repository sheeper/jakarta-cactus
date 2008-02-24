/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.ServletURL;

/**
 * Provide implementation of 
 * {@link javax.servlet.http.HttpServletRequest} for the Servlet 2.4 
 * API specifications.
 *
 * @see AbstractHttpServletRequestWrapper23
 * @version $Id: HttpServletRequestWrapper.java 238993 2004-05-22 16:39:34Z vmassol $
 */
public class HttpServletRequestWrapper 
    extends AbstractHttpServletRequestWrapper23
{
    /**
     * {@inheritDoc}
     * @see AbstractHttpServletRequestWrapper23#AbstractHttpServletRequestWrapper23(HttpServletRequest, ServletURL)
     */
    public HttpServletRequestWrapper(HttpServletRequest theRequest, 
        ServletURL theURL)
    {
        super(theRequest, theURL);
    }

    // Unmodified methods --------------------------------------------------

    /**
     * {@inheritDoc}
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    public int getRemotePort()
    {
        // TODO: Support simulation URL
        return this.request.getRemotePort(); 
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    public String getLocalName()
    {
        // TODO: Support simulation URL
        return this.request.getLocalName(); 
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    public String getLocalAddr()
    {
        // TODO: Support simulation URL
        return this.request.getLocalAddr(); 
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    public int getLocalPort()
    {
        // TODO: Support simulation URL
        return this.request.getLocalPort(); 
    }
}
