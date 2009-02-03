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
package org.apache.cactus.integration.maven2.test;

import java.io.File;

import org.apache.cactus.maven2.mojos.CactifyWarMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * Some unit tests for the <code>CactifyWarMojo</code> class.
 * 
 * @version $Id: TestCactifyWarMojo.java 238816 2004-02-29 16:36:46Z ptahchiev$
 */
public class TestCactifyWarMojo extends AbstractMojoTestCase 
{
    /**
     * The cactifyWarMojo instance.
     */
    private CactifyWarMojo cactifyWarMojo;
    
    /**
     * Set-up method to instantiate the mojo.
     * 
     * @throws Exception in case something bad happens
     */
    public void setUp() throws Exception
    {
        super.setUp();
        this.cactifyWarMojo = new CactifyWarMojo();
    }

    /**
     * Try to execute the mojo with no parameters set.
     * We should expect an exception to be raised.
     * 
     * @throws MojoExecutionException in case something bad happens
     * @throws MojoFailureException in case something bad happens
     */
    public void testNeitherSrcNorVersionAttributeSuppliedAtInstantiation() 
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            cactifyWarMojo.execute();
            fail("Should not come here.");
        }
        catch (MojoExecutionException mojex)
        {
            assertEquals("You need to specify either the [srcFile] " +
                    "or the [version] attribute", mojex.getMessage());
        }
    }
    
    /**
     * Test if you supply no arguments in the pom.xml you will get an 
     * exception.
     * 
     * @throws Exception in case some error occurs
     */
    public void testNoParametersSupplied()
    throws Exception
    {
        File testPom = new File(getBasedir(), "target/test-classes/unit/basic-"
               + "cactify-noparameters/plugin-config.xml");
        CactifyWarMojo mojo = (CactifyWarMojo) lookupMojo("cactifywar", 
                testPom);
        assertNotNull(mojo);
        try
        {
            mojo.execute();
        }
        catch (MojoExecutionException mojex)
        {
            assertEquals("You need to specify either the [srcFile] " +
                    "or the [version] attribute", mojex.getMessage());
        }
    }
    
    /**
     * Test if an empty source file (with no web.xml inside) is supplied 
     * then an adequate exception is thrown.
     * 
     * @throws Exception
     */
//    public void testNoWebXmlSuppliedInSrcFile() throws Exception
//    {
//        File testPom = new File(getBasedir(), "target/test-classes/unit/"
//               + "cactify-test-empty-src-file/plugin-config.xml");
//        CactifyWarMojo mojo = (CactifyWarMojo) lookupMojo("cactifywar", 
//               testPom);
//        assertNotNull(mojo);
//        try
//        {
//            mojo.execute();
//        }
//        catch(MojoExecutionException mx)
//        {
//            assertEquals("Failed to get the original web.xml", 
//                 mx.getMessage());  
//        }
//    }
    
    /**
     * Test that if we specify wrong version an appropriate excption is thrown.
     * 
     * @throws Exception in case some error occurs
     */
    public void testIfVersionIsSetAndWrongMergeXmlParameterPassed() 
    throws Exception
    {
        File testPom = new File(getBasedir(), "target/test-classes/unit/"
                + "cactify-test-wrong-mergexml-specified/plugin-config.xml");
         CactifyWarMojo mojo = (CactifyWarMojo) lookupMojo("cactifywar", 
                  testPom);
         assertNotNull(mojo);
         try
         {
             mojo.execute();
         }
         catch (MojoExecutionException mx)
         {
             assertEquals("Could not merge deployment descriptors", 
                     mx.getMessage());  
         }
    }
    
    /**
     * Test that if we specify wrong srcFile parameter appropriate 
     * exception is thrown.
     * 
     * @throws Exception in case some error occurs
     */
    public void testWrongSrcParameterPassed() throws Exception
    {
        File testPom = new File(getBasedir(), "target/test-classes/unit/"
                + "cactify-test-wrong-src-parameter-passed/plugin-config.xml");
         CactifyWarMojo mojo = (CactifyWarMojo) lookupMojo("cactifywar", 
                  testPom);
         assertNotNull(mojo);
         try
         {
             mojo.execute();
         }
         catch (MojoExecutionException mx)
         {
             assertEquals("Failed to get the original web.xml", 
                     mx.getMessage());
         }
    }
    
//    /**
//     * Test that if we specify wrong srcFile parameter appropriate 
//     * exception is thrown.
//     * @throws Exception
//     */
//    public void testWrongDestFileParameterPassed() throws Exception
//    {
//        File testPom = new File(getBasedir(), "target/test-classes/unit/"
//                + "cactify-test-wrong-dest-parameter-passed/plugin-config.xml");
//         CactifyWarMojo mojo = (CactifyWarMojo) lookupMojo("cactifywar", 
//                  testPom);
//         assertNotNull(mojo);
//         try
//         {
//             mojo.execute();
//         }
//         catch(MojoExecutionException mx)
//         {
//             //mx.printStackTrace();
//             assertEquals("Failed to get the original web.xml", 
//                 mx.getMessage());  
//         }
//    }
}
