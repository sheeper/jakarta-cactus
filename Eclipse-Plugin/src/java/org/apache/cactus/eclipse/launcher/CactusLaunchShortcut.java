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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.util.TestSearchEngine;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

/**
 * Launch shortcut used to start the Cactus launch configuration on the
 * current workbench selection.
 * 
 * @version $Id: $
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 */
public class CactusLaunchShortcut extends JUnitLaunchShortcut
{
    /**
     * @return the Cactus launch configuration type. This method overrides
     *         the one in {@link JUnitLaunchShortcut} so that we can return
     *         a Cactus configuration type and not a JUnit one
     */
    protected ILaunchConfigurationType getJUnitLaunchConfigType()
    {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        return lm.getLaunchConfigurationType(
            CactusLaunchConfiguration.ID_CACTUS_APPLICATION);
    }

    protected void launchType(Object[] search, String mode)
    {
        IType[] types = null;
        try
        {
            types =
                TestSearchEngine.findTests(
                    new ProgressMonitorDialog(getShell()),
                    search);
        }
        catch (InterruptedException e)
        {
            JUnitPlugin.log(e);
            return;
        }
        catch (InvocationTargetException e)
        {
            JUnitPlugin.log(e);
            return;
        }
        IType type = null;
        if (types.length == 0)
        {
            MessageDialog.openInformation(getShell(), JUnitMessages.getString("LaunchTestAction.dialog.title"), JUnitMessages.getString("LaunchTestAction.message.notests")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if (types.length > 1)
        {
            type = chooseType(types, mode);
        }
        else
        {
            type = types[0];
        }
        if (type != null)
        {
            launch(type, mode);
        }
    }

    private void launch(IType type, String mode)
    {
        ILaunchConfiguration config = findLaunchConfiguration(type, mode);
        System.out.println("Setting up the container");
        System.out.println("Starting the container");
        launchConfiguration(mode, config);
        System.out.println("Stoping the container");
        System.out.println("Cleaning the container");
    }

    private void launchConfiguration(String mode, ILaunchConfiguration config)
    {
        try
        {
            if (config != null)
            {
                DebugUITools.saveAndBuildBeforeLaunch();
                config.launch(mode, null);
            }
        }
        catch (CoreException e)
        {
            ErrorDialog.openError(getShell(), JUnitMessages.getString("LaunchTestAction.message.launchFailed"), e.getMessage(), e.getStatus()); //$NON-NLS-1$
        }
    }
}
