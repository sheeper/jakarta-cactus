/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.cactus.eclipse.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfiguration;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/**
 * Provides a launcher to start Cactus tests. This is done by implementing
 * the Eclipse
 * {@link org.eclipse.debug.core.model.ILaunchConfigurationDelegate} class
 * and adding a
 * <code>"org.eclipse.debug.core.launchConfigurationTypes"</code> extension
 * point.
 *
 * Moreover, as Cactus tests are also JUnit tests and must be started by
 * a JUnit Test Runner, this class extends the JUnit plugin
 * {@link org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfiguration}
 * launcher class. 
 * 
 * @version $Id: $
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 */
public class CactusLaunchConfiguration extends JUnitLaunchConfiguration
{
    public static final String ID_CACTUS_APPLICATION = 
        "org.apache.cactus.eclipse.launchconfig";

    /**
     * Create a VM configuration which will be used to run the Cactus tests.
     * The only difference between the Cactus plugin and JUnit plugin in term
     * of launch configuration is the VM configuration. Cactus needs more
     * jars in the classpath and need to pass additional VM arguments (the
     * Cactus configuration).
     *
     * Note: This method is called by the
     * {@link JUnitBaseLaunchConfiguration#launch()} method. That 
     * <code>launch()</code> method is the entry point of this extension point
     * and is called by Eclipse when the user press the run button.
     * 
     * @param theConfiguration the launch configuration
     * @param theTestTypes the JUnit tests that will be run
     * @param thePort the JUnit remote port
     * @param theRunMode the run mode (debug, run)
     * @return the configuration for the VM in which to run the tests 
     * 
     * @exception CoreException on critical failures
     */
    protected VMRunnerConfiguration createVMRunner(
        ILaunchConfiguration theConfiguration, IType[] theTestTypes, 
        int thePort, String theRunMode) throws CoreException
    {
        // Get the VM used by the JUnit plugin
        VMRunnerConfiguration junitVmConfig = super.createVMRunner(
            theConfiguration, theTestTypes, thePort, theRunMode);
            
        // Compute new classpath : JUnit CP + Cactus CP        
        // TODO: Add Cactus specific jars to the classpath here
        String[] cactusClasspath = {};     
        String[] classpath = concatenateStringArrays(
            junitVmConfig.getClassPath(), cactusClasspath);

        // Compute new VM arguments : JUnit VM argument + Cactus VM arguments
        // TODO: Get this from the plugin preference page.
        String[] cactusVmArgs = { 
            "-Dcactus.contextURL=http://localhost:8081/test" 
        };
        String[] vmArgs = concatenateStringArrays(
            junitVmConfig.getVMArguments(), cactusVmArgs);
                         
        // Create a new VM that includes both JUnit parameters and the new
        // Cactus parameters.
        VMRunnerConfiguration cactusVmConfig = new VMRunnerConfiguration(
            junitVmConfig.getClassToLaunch(), classpath);        
        cactusVmConfig.setBootClassPath(junitVmConfig.getBootClassPath());
        cactusVmConfig.setProgramArguments(
            junitVmConfig.getProgramArguments());
        cactusVmConfig.setVMArguments(vmArgs);
        cactusVmConfig.setVMSpecificAttributesMap(
            junitVmConfig.getVMSpecificAttributesMap());        
        cactusVmConfig.setWorkingDirectory(
            junitVmConfig.getWorkingDirectory());        

        return cactusVmConfig;
    }

    /**
     * Concatenate two string arrays.
     * 
     * @param theArray1 the first array
     * @param theArray2 the second array
     * @return a string array containing the first array followed by the second
     *         one
     */    
    private String[] concatenateStringArrays(String[] theArray1, 
        String[] theArray2)
    {
        String[] newArray = new String[theArray1.length + theArray2.length];
        System.arraycopy(theArray1, 0, newArray, 0, theArray1.length);
        System.arraycopy(theArray2, 0, newArray, theArray1.length, 
            theArray2.length);
        return newArray;
    }

}
