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

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import org.apache.cactus.ServletURL;

/**
 * Wrapper around <code>PageContext</code> so that get methods that would
 * normally return implicit objects will now return Cactus wrapper of
 * implicit objects instead.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class PageContextWrapper extends AbstractPageContextWrapper
{
    /**
     * Construct an <code>PageContext</code> instance that delegates
     * it's method calls to the page context object passed as parameter and
     * that uses the URL passed as parameter to simulate a URL from which
     * the request would come from.
     *
     * @param theOriginalPageContext the real page context
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public PageContextWrapper(PageContext theOriginalPageContext, 
        ServletURL theURL)
    {
        super(theOriginalPageContext, theURL);
    }

    // Unmodified overridden methods -----------------------------------------

    /**
     * @see PageContext#handlePageException(Throwable)
     */
    public void handlePageException(Throwable theThrowable)
        throws ServletException, IOException
    {
        this.originalPageContext.handlePageException(theThrowable);
    }
}
