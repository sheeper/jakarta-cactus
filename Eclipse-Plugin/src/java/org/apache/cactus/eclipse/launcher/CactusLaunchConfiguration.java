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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfiguration;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.internal.junit.util.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

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
     * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
     */
    public void launch(ILaunchConfiguration theConfiguration, String theMode,
        ILaunch theLaunch, IProgressMonitor thePm) throws CoreException
    {
        IJavaProject javaProject = getJavaProject(theConfiguration);
        if ((javaProject == null) || !javaProject.exists())
        {
            abort(JUnitMessages.getString(
                "JUnitBaseLaunchConfiguration.error.invalidproject"), null, 
                IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
        }

        // Get the first Cactus/JUnit test to run.
        // TODO: Add support for several tests to be run at once (needs 
        // Eclipse 2.1).
        IType testType = getFirstTestType(theConfiguration, javaProject, 
            thePm);

        IVMInstallType type = getVMInstallType(theConfiguration);
        IVMInstall install = getVMInstall(theConfiguration);
        IVMRunner runner = install.getVMRunner(theMode);

        if (runner == null)
        {
            abort(MessageFormat.format(JUnitMessages.getString(
                "JUnitBaseLaunchConfiguration.error.novmrunner"),
                new String[] { install.getId()}), null, 
                IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
        }

        File workingDir = verifyWorkingDirectory(theConfiguration);
        String workingDirName = null;
        if (workingDir != null)
        {
            workingDirName = workingDir.getAbsolutePath();
        }

        // Program & VM args
        String vmArgs = getVMArguments(theConfiguration);
        ExecutionArguments execArgs = new ExecutionArguments(vmArgs, "");

        // Create VM config
        IType types[] = { testType };
        int port = SocketUtil.findUnusedLocalPort("", 5000, 15000);

        VMRunnerConfiguration runConfig =
            createVMRunner(theConfiguration, types, port, theMode);

        // Surprisingly enough the JUnit plugin discards all VM arguments set 
        // in the configuration. This is why we add the cactus VM argument here
        String[] cactusVMArgs = runConfig.getVMArguments();
        String[] configVMArgs = execArgs.getVMArgumentsArray();
        String[] globalVMArgs =
            new String[cactusVMArgs.length + configVMArgs.length];
        System.arraycopy(configVMArgs, 0, globalVMArgs, 0, configVMArgs.length);
        System.arraycopy(cactusVMArgs, 0, globalVMArgs, configVMArgs.length, 
            cactusVMArgs.length);

        runConfig.setVMArguments(globalVMArgs);
        runConfig.setWorkingDirectory(workingDirName);

        String[] bootpath = getBootpath(theConfiguration);
        runConfig.setBootClassPath(bootpath);

        //  set default source locator if none specified
        String id =
            theConfiguration.getAttribute(
                ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
                (String) null);
        if (id == null)
        {
            ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
            theLaunch.setSourceLocator(sourceLocator);
        }

        theLaunch.setAttribute(PORT_ATTR, Integer.toString(port));
        theLaunch.setAttribute(TESTTYPE_ATTR, testType.getHandleIdentifier());
        runner.run(runConfig, theLaunch, thePm);
    }

    /**
     * Create a VM which will be used to run the Cactus tests. This VM is 
     * created by getting the JUnit plugin VM and adding Cactus specific
     * parameters to it. This method overrides the JUnit Plugin one.
     */
    protected VMRunnerConfiguration createVMRunner(
        ILaunchConfiguration theConfiguration, IType[] theTestTypes, 
        int thePort, String theRunMode) throws CoreException
    {
        // Get the VM used by the JUnit plugin
        VMRunnerConfiguration vmConfig = super.createVMRunner(theConfiguration,        
            theTestTypes, thePort, theRunMode);
            
        // Add Cactus specific arguments
        // TODO: Get this from the plugin preference page.
        String[] vmArgs = { "-Dcactus.contextURL=http://localhost:8081/test" };
        vmConfig.setVMArguments(vmArgs);

        return vmConfig;
    }

    /**
     * Adds the junitsupport.jar path to the given configuration and returns 
     * it.
     * 
     * @param theConfiguration
     * @param theType
     * @return
     * @throws CoreException
     */
    private String[] createClassPath(ILaunchConfiguration theConfiguration, 
        IType theType) throws CoreException
    {
        URL url = JUnitPlugin.getDefault().getDescriptor().getInstallURL();
        String[] cp = getClasspath(theConfiguration);
        boolean inDevelopmentMode = BootLoader.inDevelopmentMode();

        String[] classPath = new String[cp.length + 1];
        System.arraycopy(cp, 0, classPath, 1, cp.length);
        try
        {
            if (inDevelopmentMode)
            {
                // assumption is that the output folder is called bin!
                classPath[0] = Platform.asLocalURL(
                    new URL(url, "bin")).getFile();
            } 
            else
            {
                classPath[0] = Platform.asLocalURL(
                    new URL(url, "junitsupport.jar")).getFile();
            }
        } 
        catch (MalformedURLException e)
        {
            JUnitPlugin.log(e); // TO DO abort run and inform user
        } 
        catch (IOException e)
        {
            JUnitPlugin.log(e); // TO DO abort run and inform user
        }
        return classPath;
    }

    /**
     * Helper method to make our class work both in Eclipse 2.0 and 2.1. Indeed,
     * in Eclipse 2.1, the JUnit plugin has changed and the 
     * <code>getTestType()</code> method has been removed in favor of a 
     * <code>getTestTypes()</code> to support running several tests at once.
     * 
     * @param theConfiguration the configuration to launch
     * @param theJavaProject reference to the current active java project
     * @param thePm the progress monitor, or <code>null</code>
     * @return the test to run. In Eclipse 2.1 returns the first test selected
     */
    private IType getFirstTestType(ILaunchConfiguration theConfiguration,
        IJavaProject theJavaProject, IProgressMonitor thePm)
    {                
        // TODO: Upon exception, stop the launch so that this method should
        // never have to return null.
                
        IType testType = null;
        
        try
        {
            // Test if the getTestType() method exist. It does for Eclipse 2.0
            // but not for Eclipse 2.1
            Method getTestTypeMethod = this.getClass().getMethod("getTestType", 
                new Class[] { ILaunchConfiguration.class, 
                IJavaProject.class });
            try
            {
                testType = (IType) getTestTypeMethod.invoke(this, 
                    new Object[] { theConfiguration, theJavaProject });
            }
            catch (Exception e)
            {
                JUnitPlugin.log(e); // TO DO abort run and inform user
            }
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                // Ok, this means we should be in Eclipse 2.1
                Method getTestTypeMethod = this.getClass().getMethod(
                    "getTestTypes", new Class[] { ILaunchConfiguration.class, 
                    IJavaProject.class, IProgressMonitor.class });
                try
                {
                    testType = ((IType[]) getTestTypeMethod.invoke(this, 
                        new Object[] { theConfiguration, theJavaProject, 
                        thePm }))[0];
                }
                catch (Exception ee)
                {
                    JUnitPlugin.log(ee); // TO DO abort run and inform user
                }
            }
            catch (NoSuchMethodException ee)
            {
                JUnitPlugin.log(ee); // TO DO abort run and inform user
            }
 
        }

        return testType;
    }
    
}
