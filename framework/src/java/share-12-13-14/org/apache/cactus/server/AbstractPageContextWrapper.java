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

import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import org.apache.cactus.ServletURL;

/**
 * Abstract wrapper around <code>PageContext</code>. This class provides
 * a common implementation of the wrapper for the different servlet API.
 *
 * @version $Id$
 */
public abstract class AbstractPageContextWrapper extends PageContext
{
    /**
     * The real page context
     */
    protected PageContext originalPageContext;

    /**
     * The URL to simulate
     */
    protected ServletURL url;

    /**
     * Construct an <code>PageContext</code> instance that delegates
     * it's method calls to the page context object passed as parameter and
     * that uses the URL passed as parameter to simulate a URL from which
     * the request would come from.
     *
     * @param theOriginalPageContext the real page context
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public AbstractPageContextWrapper(PageContext theOriginalPageContext, 
        ServletURL theURL)
    {
        this.originalPageContext = theOriginalPageContext;
        this.url = theURL;
    }

    // Modified overridden methods -------------------------------------------

    /**
     * @return the Cactus wrapped servlet request that knows about the
     *         simulated URL
     */
    public ServletRequest getRequest()
    {
        // Note: we only manage HttpServletRequest here
        return new HttpServletRequestWrapper(
            (HttpServletRequest) this.originalPageContext.getRequest(), 
            this.url);
    }

    /**
     * @return the Cactus wrapped servlet config
     */
    public ServletConfig getServletConfig()
    {
        return new ServletConfigWrapper(
            this.originalPageContext.getServletConfig());
    }

    /**
     * @return the Cactus wrapped servlet context
     */
    public ServletContext getServletContext()
    {
        return new ServletContextWrapper(
            this.originalPageContext.getServletContext());
    }

    // Unmodified overridden methods -----------------------------------------

    /**
     * @see PageContext#findAttribute(String)
     */
    public Object findAttribute(String theName)
    {
        return this.originalPageContext.findAttribute(theName);
    }

    /**
     * @see PageContext#forward(String)
     */
    public void forward(String theRelativeURLPath) throws ServletException, 
        IOException
    {
        this.originalPageContext.forward(theRelativeURLPath);
    }

    /**
     * @see PageContext#getAttribute(String)
     */
    public Object getAttribute(String theName)
    {
        return this.originalPageContext.getAttribute(theName);
    }

    /**
     * @see PageContext#getAttribute(String, int)
     */
    public Object getAttribute(String theName, int theScope)
    {
        return this.originalPageContext.getAttribute(theName, theScope);
    }

    /**
     * @see PageContext#getAttributeNamesInScope(int)
     */
    public Enumeration getAttributeNamesInScope(int theScope)
    {
        return this.originalPageContext.getAttributeNamesInScope(theScope);
    }

    /**
     * @see PageContext#getAttributesScope(String)
     */
    public int getAttributesScope(String theName)
    {
        return this.originalPageContext.getAttributesScope(theName);
    }

    /**
     * @see PageContext#getException()
     */
    public Exception getException()
    {
        return this.originalPageContext.getException();
    }

    /**
     * @see PageContext#getOut()
     */
    public JspWriter getOut()
    {
        return this.originalPageContext.getOut();
    }

    /**
     * @see PageContext#getPage()
     */
    public Object getPage()
    {
        return this.originalPageContext.getPage();
    }

    /**
     * @see PageContext#getResponse()
     */
    public ServletResponse getResponse()
    {
        return this.originalPageContext.getResponse();
    }

    /**
     * @see PageContext#getSession()
     */
    public HttpSession getSession()
    {
        return this.originalPageContext.getSession();
    }

    /**
     * @see PageContext#handlePageException(Exception)
     */
    public void handlePageException(Exception theException)
        throws ServletException, IOException
    {
        this.originalPageContext.handlePageException(theException);
    }

    /**
     * @see PageContext#include(String)
     */
    public void include(String theRelativeURLPath) throws ServletException, 
        IOException
    {
        this.originalPageContext.include(theRelativeURLPath);
    }

    /**
     * @see PageContext#initialize
     */
    public void initialize(Servlet theServlet, ServletRequest theRequest, 
        ServletResponse theResponse, String theErrorPageURL, 
        boolean isSessionNeeded, int theBufferSize, boolean isAutoFlush) 
        throws IOException, IllegalStateException, IllegalArgumentException
    {
        this.originalPageContext.initialize(theServlet, theRequest, 
            theResponse, theErrorPageURL, isSessionNeeded, theBufferSize, 
            isAutoFlush);
    }

    /**
     * @see PageContext#popBody()
     */
    public JspWriter popBody()
    {
        return this.originalPageContext.popBody();
    }

    /**
     * @see PageContext#pushBody()
     */
    public BodyContent pushBody()
    {
        return this.originalPageContext.pushBody();
    }

    /**
     * @see PageContext#release()
     */
    public void release()
    {
        this.originalPageContext.release();
    }

    /**
     * @see PageContext#removeAttribute(String)
     */
    public void removeAttribute(String theName)
    {
        this.originalPageContext.removeAttribute(theName);
    }

    /**
     * @see PageContext#removeAttribute(String, int)
     */
    public void removeAttribute(String theName, int theScope)
    {
        this.originalPageContext.removeAttribute(theName, theScope);
    }

    /**
     * @see PageContext#setAttribute(String, Object)
     */
    public void setAttribute(String theName, Object theAttribute)
    {
        this.originalPageContext.setAttribute(theName, theAttribute);
    }

    /**
     * @see PageContext#setAttribute(String, Object)
     */
    public void setAttribute(String theName, Object theAttribute, int theScope)
    {
        this.originalPageContext.setAttribute(theName, theAttribute, theScope);
    }
}
