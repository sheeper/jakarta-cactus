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
 * This class contains the configuration for the VM that will actually launch 
 * the cactus tests.
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

        IType testType = getTestType(theConfiguration, javaProject);
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
     * @see org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration#createVMRunner(ILaunchConfiguration, IType[], int, String)
     */
    protected VMRunnerConfiguration createVMRunner(
        ILaunchConfiguration theConfiguration, IType[] theTestTypes, 
        int thePort, String theRunMode) throws CoreException
    {
        String[] classPath = createClassPath(theConfiguration, theTestTypes[0]);
        VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
            "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", 
            classPath);

        Vector argv = new Vector(10);
        argv.add("-port");
        argv.add(Integer.toString(thePort));
        argv.add("-classNames");

        if (keepAlive(theConfiguration) 
            && theRunMode.equals(ILaunchManager.DEBUG_MODE))
        {
            argv.add(0, "-keepalive");
        }
        
        for (int i = 0; i < theTestTypes.length; i++)
        {
            argv.add(theTestTypes[i].getFullyQualifiedName());
        }
        
        String[] args = new String[argv.size()];
        argv.copyInto(args);
        vmConfig.setProgramArguments(args);

        // We set a VM argument related to the Cactus framework (QQQ : get 
        // this from the plugin preference page).
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
}
