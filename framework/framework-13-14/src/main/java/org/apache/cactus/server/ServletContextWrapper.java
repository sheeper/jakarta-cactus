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

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Wrapper around Servlet 2.3 <code>ServletContext</code>. This wrapper
 * provides additional behaviour (see
 * <code>AbstractServletContextWrapper</code>).
 *
 * @version $Id: ServletContextWrapper.java 238991 2004-05-22 11:34:50Z vmassol $
 * @see RequestDispatcherWrapper
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

    /**
     * {@inheritDoc}
     * @see ServletContext#getServletContextName()
     */
    public String getServletContextName()
    {
        return this.originalContext.getServletContextName();
    }

    /**
     * {@inheritDoc}
     * @see #getResourcePaths(String)
     */
    public Set getResourcePaths()
    {
        Set returnSet;

        // Use reflection because newest Servlet API 2.3 changes removed this
        // method
        try
        {
            Method method = this.originalContext.getClass().getMethod(
                "getResourcePaths", null);

            if (method != null)
            {
                returnSet = (Set) method.invoke(this.originalContext, null);
            }
            else
            {
                throw new RuntimeException("Method ServletContext."
                    + "getResourcePaths() no longer supported by your servlet "
                    + "engine !");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error getting/calling method "
                + "getResourcePaths()");
        }

        return returnSet;
    }

    /**
     * Added to support the changes of the Jakarta Servlet API 2.3 of the
     * 17/03/2001 (in anticipation of the upcoming draft of Servlet 2.3). Kept
     * the method without parameters for servlet engines that do not have
     * upgraded yet to the new signature.
     *
     * {@inheritDoc}
     * @see ServletContext#getResourcePaths(String)
     */
    public Set getResourcePaths(String thePath)
    {
        Set returnSet;

        // Check if the method exist (for servlet engines that do not have
        // upgraded yet)
        try
        {
            Method method = this.originalContext.getClass().getMethod(
                "getResourcePaths", new Class[] {String.class});

            if (method != null)
            {
                returnSet = (Set) method.invoke(this.originalContext, 
                    new Object[] {thePath});
            }
            else
            {
                throw new RuntimeException("Method ServletContext."
                    + "getResourcePaths(String path) not supported yet by your "
                    + "servlet engine !");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error getting/calling method "
                + "getResourcePaths(String path)");
        }

        return returnSet;
    }
}
