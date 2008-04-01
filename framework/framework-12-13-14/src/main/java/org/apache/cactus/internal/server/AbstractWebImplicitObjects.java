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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Holder class that contains the instances of the implicit objects that exist
 * for all web requests. Namely they are <code>HttpServletRequest</code>,
 * <code>HttpServletResponse</code> and <code>ServletContext</code>.
 *
 * @version $Id: AbstractWebImplicitObjects.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public abstract class AbstractWebImplicitObjects implements WebImplicitObjects
{
    /**
     * The HTTP request object.
     */
    protected HttpServletRequest request;

    /**
     * The HTTP response object.
     */
    protected HttpServletResponse response;

    /**
     * The Context object.
     */
    protected ServletContext context;

    /**
     * @return the <code>ServletContext</code> implicit object
     */
    public ServletContext getServletContext()
    {
        return this.context;
    }

    /**
     * @param theContext the <code>ServletContext</code> implicit object
     */
    public void setServletContext(ServletContext theContext)
    {
        this.context = theContext;
    }

    /**
     * @return the <code>HttpServletResponse</code> implicit object
     */
    public HttpServletResponse getHttpServletResponse()
    {
        return this.response;
    }

    /**
     * @param theResponse the <code>HttpServletResponse</code> implicit object
     */
    public void setHttpServletResponse(HttpServletResponse theResponse)
    {
        this.response = theResponse;
    }

    /**
     * @return the <code>HttpServletRequest</code> implicit object
     */
    public HttpServletRequest getHttpServletRequest()
    {
        return this.request;
    }

    /**
     * @param theRequest the <code>HttpServletRequest</code> implicit object
     */
    public void setHttpServletRequest(HttpServletRequest theRequest)
    {
        this.request = theRequest;
    }
}
