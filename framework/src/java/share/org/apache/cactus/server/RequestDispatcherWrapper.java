/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 The Apache Software Foundation.  All rights
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
package org.apache.cactus.server;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Wrapper around <code>RequestDispatcher</code> which overrides the
 * <code>forward()</code> and <code>include</code> methods to use the original
 * HTTP request object instead of the simulated one used by Cactus.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class RequestDispatcherWrapper implements RequestDispatcher
{
    /**
     * The original request dispatcher object
     */
    private RequestDispatcher originalDispatcher;

    /**
     * @param theOriginalDispatcher the original request dispatcher object
     */
    public RequestDispatcherWrapper(RequestDispatcher theOriginalDispatcher)
    {
        this.originalDispatcher = theOriginalDispatcher;
    }

    /**
     * Call the original <code>RequestDispatcher</code> <code>forward()</code>
     * method but with the original HTTP request (not the simulation one which
     * would make the servlet engine choke !).
     *
     * @param theRequest the simulation HTTP request
     * @param theResponse the original HTTP response
     * @exception IOException {@link RequestDispatcher#forward}
     * @exception ServletException {@link RequestDispatcher#forward}
     */
    public void forward(ServletRequest theRequest, ServletResponse theResponse)
        throws IOException, ServletException
    {
        // Always pass the original request to the forward() call.
        if (HttpServletRequestWrapper.class.isAssignableFrom(
            theRequest.getClass()))
        {
            HttpServletRequestWrapper request = 
                (HttpServletRequestWrapper) theRequest;

            this.originalDispatcher.forward(request.getOriginalRequest(),
                theResponse);
        }
        else
        {
            this.originalDispatcher.forward(theRequest, theResponse);
        }
    }

    /**
     * Call the original <code>RequestDispatcher</code> <code>include()</code>
     * method but with the original HTTP request (not the simulation one which
     * would make the servlet engine choke !).
     *
     * @param theRequest the simulation HTTP request
     * @param theResponse the original HTTP response
     * @exception IOException {@link RequestDispatcher#forward}
     * @exception ServletException {@link RequestDispatcher#forward}
     */
    public void include(ServletRequest theRequest, ServletResponse theResponse)
        throws IOException, ServletException
    {
        // Always pass the original request to the forward() call.
        if (!HttpServletRequestWrapper.class.isAssignableFrom(
            theRequest.getClass()))
        {
            HttpServletRequestWrapper request = 
                (HttpServletRequestWrapper) theRequest;

            this.originalDispatcher.include(request.getOriginalRequest(), 
                theResponse);
        }
        else
        {
            this.originalDispatcher.include(theRequest, theResponse);
        }
    }
}
