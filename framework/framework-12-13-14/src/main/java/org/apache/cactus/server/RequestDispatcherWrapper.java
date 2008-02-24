/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Wrapper around <code>RequestDispatcher</code> which overrides the
 * <code>forward()</code> and <code>include</code> methods to use the original
 * HTTP request object instead of the simulated one used by Cactus.
 *
 * @version $Id: RequestDispatcherWrapper.java 292559 2005-09-29 21:36:43Z kenney $
 */
public class RequestDispatcherWrapper implements RequestDispatcher
{
    /**
     * The original request dispatcher object.
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
        if (theRequest instanceof AbstractHttpServletRequestWrapper)
        {
            AbstractHttpServletRequestWrapper request = 
                (AbstractHttpServletRequestWrapper) theRequest;

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
        if (theRequest instanceof AbstractHttpServletRequestWrapper)
        {
            AbstractHttpServletRequestWrapper request = 
                (AbstractHttpServletRequestWrapper) theRequest;

            this.originalDispatcher.include(request.getOriginalRequest(), 
                theResponse);
        }
        else
        {
            this.originalDispatcher.include(theRequest, theResponse);
        }
    }
}
