/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.server;

import java.net.URLDecoder;

import org.apache.cactus.util.ChainedRuntimeException;

/**
 * All prupose utility methods for manipulating the Servlet API.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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