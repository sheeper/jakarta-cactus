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

import java.net.URLDecoder;

import org.apache.cactus.util.ChainedRuntimeException;

/**
 * All prupose utility methods for manipulating the Servlet API.
 *
 * @version $Id$
 */
public class ServletUtil
{
    /**
     * A substitute method for <code>HttpServletRequest.getParameter()</code>.
     * Contrary to <code>getParameter()</code>, this method does not
     * access the request input stream (only the query string of the url).
     *
     * Note: We use this method internally to retrieve Cactus parameters passed
     * by the client side. The issue with <code>getParameter()</code> is that
     * if you use it, then you cannot call <code>getReader()</code> or
     * <code>getInputStream()</code> (see the Servlet spec). However, if we
     * want to allow for testing code that uses these 2 methods (and we do !)
     * we need to use this method to get the internal Cactus parameters.
     *
     * @param theQueryString the query string to parse
     * @param theParameter the name of the parameter to locate
     * @return the value for theParameter in theQueryString, null if
     *         theParameter does not exist and "" if the parameter exists but
     *         has no value defined in the query string
     */
    public static String getQueryStringParameter(String theQueryString, 
        String theParameter)
    {
        if (theQueryString == null)
        {
            return null;
        }

        String value = null;

        int startIndex = theQueryString.indexOf(theParameter + "=");

        if (startIndex >= 0)
        {
            // add 1 for '='
            startIndex += (theParameter.length() + 1);

            int endIndex = theQueryString.indexOf('&', startIndex);

            if (endIndex > startIndex)
            {
                value = theQueryString.substring(startIndex, endIndex);
            }
            else if (endIndex == startIndex)
            {
                value = "";
            }
            else
            {
                value = theQueryString.substring(startIndex);
            }

            // In JDK 1.2 URLDecoder.decode throws an Exception. This is not
            // needed for JDK 1.3+ but needed to keep JDK 1.2.2 compatibility
            try
            {
                value = URLDecoder.decode(value);
            }
            catch (Exception e)
            {
                throw new ChainedRuntimeException("Error URL decoding ["
                    + value + "]", e);
            }
        }

        return value;
    }
}
