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
package org.apache.cactus.internal.util;

import junit.framework.TestCase;
import org.apache.cactus.Cookie;
import org.apache.cactus.WebRequest;
import org.apache.cactus.internal.WebRequestImpl;
import org.apache.commons.httpclient.HttpState;

import java.net.URL;

/**
 * Unit tests for the {@link CookieUtil} class.
 *
 * @version $Id: TestCookieUtil.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class TestCookieUtil extends TestCase
{
    /**
     * Verify that an HttpClient HttpState object can be created when
     * no Cactus cookies have been defined.
     * 
     * @exception Exception on error
     */
    public void testCreateHttpStateWhenNoCactusCookieDefined()
        throws Exception
    {
        WebRequest request = new WebRequestImpl();
        HttpState state = new HttpState(); 
        state.addCookies(CookieUtil.createHttpClientCookies(request, 
            new URL("http://jakarta.apache.org")));
        assertEquals(0, state.getCookies().length);
    }

    /**
     * Verify that an HttpClient HttpState object can be created when
     * several Cactus cookies exist.
     * 
     * @exception Exception on error
     */
    public void testCreateHttpStateWhenSeveralCactusCookieExist()
        throws Exception
    {
        WebRequest request = new WebRequestImpl();
        request.addCookie(new Cookie("domain1", "name1", "value1"));
        request.addCookie(new Cookie("domain2", "name2", "value2"));
        
        HttpState state = new HttpState(); 
        state.addCookies(CookieUtil.createHttpClientCookies(request, 
            new URL("http://jakarta.apache.org")));

        assertEquals(2, state.getCookies().length);
    }

}
