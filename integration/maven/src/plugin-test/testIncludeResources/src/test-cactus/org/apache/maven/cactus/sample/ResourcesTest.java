/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.maven.cactus.sample;

import org.apache.cactus.ServletTestCase;
import java.io.InputStream;

public class ResourcesTest extends ServletTestCase
{
    public ResourcesTest()
    {
        super();
    }

    public ResourcesTest(String name)
    {
        super(name);
    }

    public void testConfigProperties()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("test.properties");
        assertNotNull(input);
    }
  
    public void testConfigXml()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("test.xml");
        assertNotNull(input);
    }
    
    public void testIncludes()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("testKO.xml");
        assertNull(input);
    }
    
    public void testExcludes()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("testbad.properties");
        assertNull(input);
    }

    public void testRecursive()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("recursiveResources/test-recursive.properties");
        assertNotNull(input);
    }
    
    public void testRecursiveDefault()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("recursiveResources/test-recursive-default.xml");
        assertNotNull(input);
    }
}
