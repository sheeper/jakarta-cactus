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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for implicit objects that exist
 * for all web requests (<code>HttpServletRequest</code>,
 * <code>HttpServletResponse</code> and <code>ServletContext</code>).
 *
 * @version $Id$
 */
public interface WebImplicitObjects extends ImplicitObjects
{
    /**
     * @return the <code>ServletContext</code> implicit object
     */
    ServletContext getServletContext();

    /**
     * @param theContext the <code>ServletContext</code> implicit object
     */
    void setServletContext(ServletContext theContext);

    /**
     * @return the <code>HttpServletResponse</code> implicit object
     */
    HttpServletResponse getHttpServletResponse();

    /**
     * @param theResponse the <code>HttpServletResponse</code> implicit object
     */
    void setHttpServletResponse(HttpServletResponse theResponse);

    /**
     * @return the <code>HttpServletRequest</code> implicit object
     */
    HttpServletRequest getHttpServletRequest();

    /**
     * @param theRequest the <code>HttpServletRequest</code> implicit object
     */
    void setHttpServletRequest(HttpServletRequest theRequest);
}
