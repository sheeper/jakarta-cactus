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

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
/**
 * Unit tests for the {@link UrlUtil} class.
 *
 * @version $Id: TestUrlUtil.java 239169 2005-05-05 09:21:54Z paranoiabla $
 */
public class TestUrlUtil extends TestCase
{
    /**
     * Verify that appropriate url does not result to null path.
     */
    public void testGetPathNotNull()
    {
        URL url;
        try 
        {
            url = new URL("http", "jakarta.apache.org", 80, "index.html");
            assertNotNull(UrlUtil.getPath(url));
        } 
        catch (MalformedURLException e)
        {
            fail("MailformedURLException - you are executing the test"
                + " with a mailformed url");
        }
        
    }
    
    /**
     * Verify that appropriate url does not result to null query.
     */
    public void testGetQueryNotNull()
    {
        URL url;
        try 
        {
            url = new URL("http", "jakarta.apache.org", 80, "index.html");
            assertNotNull(UrlUtil.getPath(url));
        } 
        catch (MalformedURLException e)
        {
            fail("MailformedURLException - you are executing the test"
                + " with a mailformed url");
        }
        
    }
    
    
}
