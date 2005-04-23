/* 
 * ========================================================================
 * 
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.cactus.integration.maven.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;


public class PropertiesTest extends TestCase
{

    public void testCreateFile() throws IOException
    {
    	String basedir = System.getProperty("basedir");
    	File f = new File( basedir + File.separator + "target" + File.separator + "tmpFile.txt" );
    	System.out.println( "Creating file " + f.getAbsolutePath() );
    	assertTrue( f.createNewFile() );
    }

}