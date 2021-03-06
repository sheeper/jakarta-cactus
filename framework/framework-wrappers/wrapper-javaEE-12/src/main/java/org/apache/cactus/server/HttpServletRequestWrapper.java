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

import org.apache.cactus.ServletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * Encapsulation class for the Servlet 2.2 API <code>HttpServletRequest</code>.
 * This is an implementation that delegates all the call to the
 * <code>HttpServletRequest</code> object passed in the constructor except for
 * some overridden methods which are use to simulate a URL. This is to be able
 * to simulate any URL that would have been used to call the test method : if
 * this was not done, the URL that would be returned (by calling the
 * <code>getRequestURI()</code> method or others alike) would be the URL of the
 * server redirector servlet or JSP and not a URL that the test case want to
 * simulate.
 *
 * @version $Id: HttpServletRequestWrapper.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class HttpServletRequestWrapper
    extends AbstractHttpServletRequestWrapper
{
    /**
     * Construct an <code>HttpServletRequest</code> instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the request
     * would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public HttpServletRequestWrapper(HttpServletRequest theRequest, 
        ServletURL theURL)
    {
        super(theRequest, theURL);
    }
}
