/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus.extension.jsp;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

/**
 * Convenience class that supports the testing of JSP tag by managing the tag's
 * lifecycle as required by the JSP specification.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 * @see org.apache.cactus.JspTestCase
 */
public class JspTagLifecycle
{  
    // Inner Classes -----------------------------------------------------------
    
    /**
     *  
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     */
    public abstract static class Interceptor
    {
        
        /**
         * Method called when the body of the tag would be evaluated. Can be
         * used in specific test cases to perform assertions.
         *  
         * @param theIteration The number of times the body has been evaluated
         * @param theBody The body content, or <tt>null</tt> if the tag isn't a
         *        <tt>BodyTag</tt>
         * @throws IOException If an error occurs when reading or writing the
         *         body content
         */
        public void evalBody(int theIteration, BodyContent theBody)
            throws IOException
        {
            // default implementation does nothing
        }
        
        /**
         * Method called when the body of the tag would be skipped. Can be used 
         * in specific test cases to perform assertions.
         */
        public void skipBody()
        {
            // default implementation does nothing
        }
        
    }
    
    /**
     * Used internally to avoid having to check for <tt>null</tt>.
     */
    private static final Interceptor NOOP_INTERCEPTOR = 
        new Interceptor()
        {
        };
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * The JSP tag handler.
     */
    private PageContext pageContext;
    
    /**
     * The JSP tag handler.
     */
    private Tag tag;
    
    /**
     * The JSP tag handler.
     */
    private Tag parent;
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param thePageContext The JSP page context
     * @param theTag The JSP tag
     */
    public JspTagLifecycle(PageContext thePageContext, Tag theTag)
    {
        this(thePageContext, theTag, null);
    }
    
    /**
     * Constructor.
     * 
     * @param thePageContext The JSP page context
     * @param theTag The JSP tag
     * @param theParent The parent tag, or <tt>null</tt>
     */
    public JspTagLifecycle(PageContext thePageContext, Tag theTag, 
        Tag theParent)
    {
        this.pageContext = thePageContext;
        this.tag = theTag;
        this.parent = theParent;
    }
    
    // Public Methods ----------------------------------------------------------
    
    /**
     * Invokes the tag in the provided page context. The tag should have been
     * populated with its properties before calling this method. The tag is not
     * released after the tag's lifecycle is over.
     * 
     * @throws JspException If the tag throws an exception
     * @throws IOException If an error occurs when reading or writing the body
     *         content
     */
    public void invoke() throws JspException, IOException
    {
        invoke(NOOP_INTERCEPTOR);
    }
    
    /**
     * Invokes the tag in the provided page context. The tag should have been
     * populated with its properties before calling this method. The tag is not
     * released after the tag's lifecycle is over.
     * 
     * @param theInterceptor The interceptor that will be notified about 
     *        lifecycle events
     * @throws JspException If the tag throws an exception
     * @throws IOException If an error occurs when reading or writing the body
     *         content
     */
    public void invoke(Interceptor theInterceptor) 
        throws JspException, IOException
    {
        if (theInterceptor == null)
        {
            throw new NullPointerException();
        }

        tag.setPageContext(pageContext);
        tag.setParent(parent);
        BodyContent body = null;

        if (tag instanceof TryCatchFinally)
        {
            TryCatchFinally tryCatchFinally = (TryCatchFinally) tag;
            try
            {
                body = invokeTag(theInterceptor);
            }
            catch (Throwable t1)
            {
                try
                {
                    tryCatchFinally.doCatch(t1);
                }
                catch (Throwable t2)
                {
                    throw new JspException(t2.getMessage());
                }
            }
            finally
            {
                if (body != null)
                {
                    pageContext.popBody();
                    body = null;
                }
                tryCatchFinally.doFinally();
            }
        }
        else
        {
            try
            {
                body = invokeTag(theInterceptor);
            }
            finally
            {
                if (body != null)
                {
                    pageContext.popBody();
                    body = null;
                }
            }
        }
    }
    
    // Private Methods ---------------------------------------------------------
    
    /**
     * Internal method to invoke a tag without doing exception handling.
     * 
     * @param theInterceptor The interceptor that will be notified about 
     *        lifecycle events
     * @throws JspException If the tag throws an exception
     * @throws IOException If an error occurs when reading or writing the body
     *         content
     * @return The body content, or <tt>null</tt> if the tag didn't request
     *         buffered body evaluation
     */
    private BodyContent invokeTag(Interceptor theInterceptor)
        throws JspException, IOException
    {
        BodyContent body = null;
        int status = tag.doStartTag();
        if (status != Tag.SKIP_BODY)
        {
            if (tag instanceof IterationTag)
            {
                IterationTag iterationTag = (IterationTag) tag;
                if ((status == BodyTag.EVAL_BODY_BUFFERED)
                    && (tag instanceof BodyTag))
                {
                    BodyTag bodyTag = (BodyTag) tag;
                    body = pageContext.pushBody();
                    bodyTag.setBodyContent(body);
                    bodyTag.doInitBody();
                }
                int iteration = 0;
                do
                {
                    theInterceptor.evalBody(iteration, body);
                    status = iterationTag.doAfterBody();
                    iteration++;
                } while (status == IterationTag.EVAL_BODY_AGAIN);
            }
        }
        else
        {
            theInterceptor.skipBody();
        }
        status = tag.doEndTag();
        return body;
    }
    
}
