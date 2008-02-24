/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.cactus.internal.AbstractCactusTestCase;
import org.apache.cactus.internal.CactusTestCase;
import org.apache.cactus.internal.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.internal.configuration.DefaultFilterConfiguration;
import org.apache.cactus.server.FilterConfigWrapper;
import org.apache.cactus.server.AbstractHttpServletRequestWrapper;
import org.apache.cactus.spi.client.connector.ProtocolHandler;

/**
 * Test classes that need access to valid Filter implicit objects (such as the
 * <code>FilterConfig</code> and <code>FilterChain</code> objects) must
 * subclass this class.
 * 
 * @version $Id: FilterTestCase.java 292560 2005-09-29 21:48:10Z kenney $
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
    public AbstractHttpServletRequestWrapper request;

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
     * {@inheritDoc}
     * @see AbstractCactusTestCase#AbstractCactusTestCase(String)
     */
    public FilterTestCase(String theName)
    {
        super(theName);
    }

    /**
     * {@inheritDoc}
     * @see AbstractCactusTestCase#AbstractCactusTestCase(String, Test)
     */
    public FilterTestCase(String theName, Test theTest)
    {
        super(theName, theTest);
    }

    /**
     * {@inheritDoc}
     * @see AbstractCactusTestCase#createProtocolHandler()
     */
    protected ProtocolHandler createProtocolHandler()
    {
        return new HttpProtocolHandler(new DefaultFilterConfiguration());
    }
   }
