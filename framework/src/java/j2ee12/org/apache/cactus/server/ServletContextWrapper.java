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

/**
 * Wrapper around Servlet 2.2 <code>ServletContext</code>. This wrapper
 * provides additional behaviour (see
 * <code>AbstractServletContextWrapper</code>).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @see AbstractServletContextWrapper
 */
public class ServletContextWrapper extends AbstractServletContextWrapper
{
    /**
     * @param theOriginalContext the original servlet context object
     */
    public ServletContextWrapper(ServletContext theOriginalContext)
    {
        super(theOriginalContext);
    }
}
