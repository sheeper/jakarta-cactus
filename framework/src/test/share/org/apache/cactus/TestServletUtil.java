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

import junit.framework.TestCase;

import org.apache.cactus.server.ServletUtil;

/**
 * Unit tests of the <code>ServletUtil</code> class.
 *
 * @version $Id$
 */
public class TestServletUtil extends TestCase
{
    /**
     * Verify than <code>getQueryStringParameterEmpty()</code> returns an
     * empty string if the parameter existe but has no value defined.
     */
    public void testGetQueryStringParameterEmpty()
    {
        String queryString = "param1=&param2=value2";
        String result = ServletUtil.getQueryStringParameter(queryString, 
            "param1");

        assertEquals("", result);
    }
}
