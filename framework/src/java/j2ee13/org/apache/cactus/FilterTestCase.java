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

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Test;

import org.apache.cactus.client.connector.ProtocolHandler;
import org.apache.cactus.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.configuration.FilterConfiguration;
import org.apache.cactus.internal.AbstractCactusTestCase;
import org.apache.cactus.internal.CactusTestCase;
import org.apache.cactus.server.FilterConfigWrapper;

/**
 * Test classes that need access to valid Filter implicit objects (such as the
 * <code>FilterConfig</code> and <code>FilterChain</code> objects) must
 * subclass this class.
 * 
 * @version $Id$
 */
public class FilterTestCase 
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
     * Valid <code>FilterConfig</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public FilterConfigWrapper config;

    /**
     * Valid <code>FilterChain</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public FilterChain filterChain;

    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase()
     */
    public FilterTestCase()
    {
        super();
    }

    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase(String)
     */
    public FilterTestCase(String theName)
    {
        super(theName);
    }

    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase(String, Test)
     */
    public FilterTestCase(String theName, Test theTest)
    {
        super(theName, theTest);
    }

    /**
     * @see AbstractCactusTestCase#createProtocolHandler()
     */
    protected ProtocolHandler createProtocolHandler()
    {
        return new HttpProtocolHandler(new FilterConfiguration());
    }
   }
