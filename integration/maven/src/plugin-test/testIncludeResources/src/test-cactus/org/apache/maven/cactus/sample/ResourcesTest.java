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

    private void includesTest( String resource )
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(resource);
        assertNotNull("could not open resource " + resource, input);
    }

    private void excludesTest( String resource )
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(resource);
        assertNull("should not have opened resource " + resource, input);
    }

    public void testConfigProperties()
    {
        includesTest( "test.properties" );
    }
  
    public void testConfigXml()
    {
        includesTest( "test.xml" );
    }
    
    public void testIncludes()
    {
        includesTest( "testKO.properties" );
    }
    
    public void testExcludes()
    {
        excludesTest( "testbad.properties" );
    }

    public void testRecursive()
    {
        includesTest( "recursiveResources/test-recursive.properties" );
    }
    
    public void testRecursiveDefault()
    {
        includesTest( "recursiveResources/test-recursive-default.xml");
    }
}
