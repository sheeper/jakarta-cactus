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
package org.apache.cactus;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Test;

import org.apache.cactus.client.connector.ProtocolHandler;
import org.apache.cactus.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.configuration.ServletConfiguration;
import org.apache.cactus.server.ServletConfigWrapper;

/**
 * Cactus test case to unit test Servlets. Test classes that need access to 
 * valid Servlet implicit objects (such as the HTTP request, the HTTP response,
 * the servlet config, ...) must subclass this class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletTestCase 
    extends AbstractCactusTestCase implements CactusTestCase
{
    /**
     * Valid <code>HttpServletRequest</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public org.apache.cactus.server.HttpServletRequestWrapper request;

    /**
     * Valid <code>HttpServletResponse</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public HttpServletResponse response;

    /**
     * Valid <code>HttpSession</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public HttpSession session;

    /**
     * Valid <code>ServletConfig</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public ServletConfigWrapper config;

    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase()
     */
    public ServletTestCase()
    {
        super();
    }

    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase(String)
     */
    public ServletTestCase(String theName)
    {
        super(theName);
    }

    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase(String, Test)
     */
    public ServletTestCase(String theName, Test theTest)
    {
        super(theName, theTest);
    }

    /**
     * @see AbstractCactusTestCase#createProtocolHandler()
     */
    protected ProtocolHandler createProtocolHandler()
    {
        return new HttpProtocolHandler(new ServletConfiguration());
    }
}
