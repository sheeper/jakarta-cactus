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
package org.apache.cactus;

import junit.framework.Test;
import org.apache.cactus.internal.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.internal.configuration.DefaultJspConfiguration;
import org.apache.cactus.server.AbstractPageContextWrapper;
import org.apache.cactus.spi.client.connector.ProtocolHandler;

import javax.servlet.jsp.JspWriter;

/**
 * Test classes that need access to valid JSP implicit objects (such as the
 * page context, the output jsp writer, the HTTP request, ...) must subclass
 * this class.
 *
 * @version $Id: JspTestCase.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class JspTestCase extends ServletTestCase
{
    /**
     * Valid <code>PageContext</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public AbstractPageContextWrapper pageContext;

    /**
     * Valid <code>JspWriter</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public JspWriter out;

    /**
     * @see ServletTestCase#ServletTestCase()
     */
    public JspTestCase()
    {
        super();
    }

    /**
     * @see ServletTestCase#ServletTestCase(String)
     * @param theName of the JspTestCase
     */
    public JspTestCase(String theName)
    {
        super(theName);
    }

    /**
     * @see ServletTestCase#ServletTestCase(String, Test)
     * @param theName of the JspTestCase
     * @param theTest of the JspTestCase
     */
    public JspTestCase(String theName, Test theTest)
    {
        super(theName, theTest);
    }

    /**
     * @see ServletTestCase#createProtocolHandler()
     * @return ProtocolHandler
     */
    protected ProtocolHandler createProtocolHandler()
    {
        return new HttpProtocolHandler(new DefaultJspConfiguration());
    }
}
