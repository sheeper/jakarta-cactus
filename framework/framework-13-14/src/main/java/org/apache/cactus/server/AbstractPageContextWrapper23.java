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

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import org.apache.cactus.ServletURL;

/**
 * Extends {@link AbstractPageContextWrapper} by adding the new methods 
 * of the Servlet 2.3 API specifications.
 *
 * @see AbstractPageContextWrapper
 * @version $Id: AbstractPageContextWrapper23.java 238993 2004-05-22 16:39:34Z vmassol $
 */
public abstract class AbstractPageContextWrapper23 
    extends AbstractPageContextWrapper
{
    /**
     * Construct a {@link PageContext} instance that delegates
     * it's method calls to the page context object passed as parameter and
     * that uses the URL passed as parameter to simulate a URL from which
     * the request would come from.
     *
     * @param theOriginalPageContext the real page context
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public AbstractPageContextWrapper23(PageContext theOriginalPageContext, 
        ServletURL theURL)
    {
        super(theOriginalPageContext, theURL);
    }

    // Unmodified overridden methods -----------------------------------------

    /**
     * {@inheritDoc}
     * @see PageContext#handlePageException(Throwable)
     */
    public void handlePageException(Throwable theThrowable)
        throws ServletException, IOException
    {
        this.originalPageContext.handlePageException(theThrowable);
    }
}
