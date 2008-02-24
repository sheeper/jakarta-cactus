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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.cactus.ServletURL;

/**
 * Provide implementation of 
 * {@link javax.servlet.jsp.PageContext} for the Servlet 2.4 
 * API specifications.
 *
 * @see AbstractPageContextWrapper23
 * @version $Id: PageContextWrapper.java 238993 2004-05-22 16:39:34Z vmassol $
 */
public class PageContextWrapper extends AbstractPageContextWrapper23
{
    /**
     * {@inheritDoc}
     * @see AbstractPageContextWrapper23#AbstractPageContextWrapper23(PageContext, ServletURL)
     */
    public PageContextWrapper(PageContext theOriginalPageContext, 
        ServletURL theURL)
    {
        super(theOriginalPageContext, theURL);
    }

    // Unmodified methods --------------------------------------------------

    /**
     * {@inheritDoc}
     * @see PageContext#include(java.lang.String, boolean)
     */
    public void include(String theRelativeUrlPath, boolean isToBeFlushed) 
        throws ServletException, IOException
    {
        // TODO: Support simulation URL
        this.originalPageContext.include(theRelativeUrlPath, isToBeFlushed);
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.jsp.JspContext#getExpressionEvaluator()
     */
    public ExpressionEvaluator getExpressionEvaluator()
    {
        return this.originalPageContext.getExpressionEvaluator();
    }

    /**
     * {@inheritDoc}
     * @see javax.servlet.jsp.JspContext#getVariableResolver()
     */
    public VariableResolver getVariableResolver()
    {
        return this.originalPageContext.getVariableResolver();
    }
}
