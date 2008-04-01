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

import java.io.IOException;

import java.lang.reflect.Constructor;

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
import org.apache.cactus.util.ChainedRuntimeException;


/**
 * Abstract wrapper around <code>PageContext</code>. This class provides
 * a common implementation of the wrapper for the different servlet API.
 *
 * @version $Id: AbstractPageContextWrapper.java 292559 2005-09-29 21:36:43Z kenney $
 */
public abstract class AbstractPageContextWrapper extends PageContext
{
    /**
     * The real page context.
     */
    protected PageContext originalPageContext;

    /**
     * The URL to simulate.
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
    /**
     * @param theOriginalPageContext obejct
     * @param theServletURL object
     * @return AbstractPageContextWrapper
     */
    public static AbstractPageContextWrapper newInstance(PageContext
        theOriginalPageContext, ServletURL theServletURL)
    {
        try
        {
            Class clazz = Class.forName(
                "org.apache.cactus.server.PageContextWrapper");
            Object[] args = new Object[] {theOriginalPageContext,
                theServletURL};

            Constructor constructor = clazz.getConstructor(new Class[] {
                PageContext.class, ServletURL.class });

            return (AbstractPageContextWrapper) constructor.newInstance(args);
        }
        catch (Throwable t)
        {
            throw new ChainedRuntimeException(
                "Failed to create PageContextWrapper", t);
        }
    }

    // New methods ---------------------------------------------------------

    /**
     * @return the original page context
     * @since 1.7
     */
    public PageContext getOriginalPageContext()
    {
        return this.originalPageContext;
    }
    
    // Modified overridden methods -------------------------------------------

    /**
     * @return the Cactus wrapped servlet request that knows about the
     *         simulated URL
     */
    public ServletRequest getRequest()
    {
        // Note: we only manage HttpServletRequest here
        return AbstractHttpServletRequestWrapper.newInstance(
            (HttpServletRequest) this.originalPageContext.getRequest(), 
            this.url);
    }

    /**
     * @return the Cactus wrapped servlet config
     */
    public ServletConfig getServletConfig()
    {
        return AbstractServletConfigWrapper.newInstance(
            this.originalPageContext.getServletConfig());
    }

    /**
     * @return the Cactus wrapped servlet context
     */
    public ServletContext getServletContext()
    {
        return AbstractServletContextWrapper.newInstance(
            originalPageContext.getServletContext());
        }

    // Unmodified overridden methods -----------------------------------------

    /**
     * {@inheritDoc}
     * @see PageContext#findAttribute(String)
     */
    public Object findAttribute(String theName)
    {
        return this.originalPageContext.findAttribute(theName);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#forward(String)
     */
    public void forward(String theRelativeURLPath) throws ServletException, 
        IOException
    {
        this.originalPageContext.forward(theRelativeURLPath);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getAttribute(String)
     */
    public Object getAttribute(String theName)
    {
        return this.originalPageContext.getAttribute(theName);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getAttribute(String, int)
     */
    public Object getAttribute(String theName, int theScope)
    {
        return this.originalPageContext.getAttribute(theName, theScope);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getAttributeNamesInScope(int)
     */
    public Enumeration getAttributeNamesInScope(int theScope)
    {
        return this.originalPageContext.getAttributeNamesInScope(theScope);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getAttributesScope(String)
     */
    public int getAttributesScope(String theName)
    {
        return this.originalPageContext.getAttributesScope(theName);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getException()
     */
    public Exception getException()
    {
        return this.originalPageContext.getException();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getOut()
     */
    public JspWriter getOut()
    {
        return this.originalPageContext.getOut();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getPage()
     */
    public Object getPage()
    {
        return this.originalPageContext.getPage();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getResponse()
     */
    public ServletResponse getResponse()
    {
        return this.originalPageContext.getResponse();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#getSession()
     */
    public HttpSession getSession()
    {
        return this.originalPageContext.getSession();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#handlePageException(Exception)
     */
    public void handlePageException(Exception theException)
        throws ServletException, IOException
    {
        this.originalPageContext.handlePageException(theException);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#include(String)
     */
    public void include(String theRelativeURLPath) throws ServletException, 
        IOException
    {
        this.originalPageContext.include(theRelativeURLPath);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     * @see PageContext#popBody()
     */
    public JspWriter popBody()
    {
        return this.originalPageContext.popBody();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#pushBody()
     */
    public BodyContent pushBody()
    {
        return this.originalPageContext.pushBody();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#release()
     */
    public void release()
    {
        this.originalPageContext.release();
    }

    /**
     * {@inheritDoc}
     * @see PageContext#removeAttribute(String)
     */
    public void removeAttribute(String theName)
    {
        this.originalPageContext.removeAttribute(theName);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#removeAttribute(String, int)
     */
    public void removeAttribute(String theName, int theScope)
    {
        this.originalPageContext.removeAttribute(theName, theScope);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#setAttribute(String, Object)
     */
    public void setAttribute(String theName, Object theAttribute)
    {
        this.originalPageContext.setAttribute(theName, theAttribute);
    }

    /**
     * {@inheritDoc}
     * @see PageContext#setAttribute(String, Object)
     */
    public void setAttribute(String theName, Object theAttribute, int theScope)
    {
        this.originalPageContext.setAttribute(theName, theAttribute, theScope);
    }
}
