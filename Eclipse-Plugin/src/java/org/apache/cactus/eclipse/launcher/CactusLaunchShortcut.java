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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.util.TestSearchEngine;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author Julien Ruaux
 *
 * This is the LaunchShortcut class used as an extension point by the plugin.
 * 
 * @version $Id: $
 */
public class CactusLaunchShortcut extends JUnitLaunchShortcut
{

    /**
     * Create & return a new configuration based on the specified <code>IType</code>.
     */
    protected ILaunchConfiguration createConfiguration(IType type)
    {
        ILaunchConfiguration config = null;
        try
        {
            ILaunchConfigurationType configType = getCactusLaunchConfigType();
            ILaunchConfigurationWorkingCopy wc =
                configType.newInstance(
                    null,
                    getLaunchManager()
                        .generateUniqueLaunchConfigurationNameFrom(
                        type.getElementName()));
            wc.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                type.getFullyQualifiedName());
            wc.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                type.getJavaProject().getElementName());
            wc.setAttribute(
                IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE,
                IDebugUIConstants.PERSPECTIVE_DEFAULT);
            wc.setAttribute(
                IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE,
                IDebugUIConstants.PERSPECTIVE_NONE);
            wc.setAttribute(
                ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
                JavaUISourceLocator.ID_PROMPTING_JAVA_SOURCE_LOCATOR);
            wc.setAttribute(
                JUnitBaseLaunchConfiguration.ATTR_KEEPRUNNING,
                false);
            config = wc.doSave();
        } catch (CoreException ce)
        {
            CactusPlugin.log(ce);
        }
        return config;
    }

    /**
     * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
     * 
     * @return a re-useable config or <code>null</code> if none
     */
    protected ILaunchConfiguration findLaunchConfiguration(
        IType type,
        String mode)
    {
        ILaunchConfigurationType configType = getCactusLaunchConfigType();
        List candidateConfigs = Collections.EMPTY_LIST;
        try
        {
            ILaunchConfiguration[] configs =
                getLaunchManager().getLaunchConfigurations(configType);
            candidateConfigs = new ArrayList(configs.length);
            for (int i = 0; i < configs.length; i++)
            {
                ILaunchConfiguration config = configs[i];
                if (config
                    .getAttribute(
                        IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                        "")
                    .equals(type.getFullyQualifiedName()))
                { //$NON-NLS-1$
                    if (config
                        .getAttribute(
                            IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                            "")
                        .equals(type.getJavaProject().getElementName()))
                    { //$NON-NLS-1$
                        candidateConfigs.add(config);
                    }
                }
            }
        } catch (CoreException e)
        {
            CactusPlugin.log(e);
        }

        // If there are no existing configs associated with the IType, create one.
        // If there is exactly one config associated with the IType, return it.
        // Otherwise, if there is more than one config associated with the IType, prompt the
        // user to choose one.
        int candidateCount = candidateConfigs.size();
        if (candidateCount < 1)
        {
            return createConfiguration(type);
        } else if (candidateCount == 1)
        {
            return (ILaunchConfiguration) candidateConfigs.get(0);
        } else
        {
            // Prompt the user to choose a config.  A null result means the user
            // cancelled the dialog, in which case this method returns null,
            // since cancelling the dialog should also cancel launching anything.
            ILaunchConfiguration config =
                chooseConfiguration(candidateConfigs, mode);
            if (config != null)
            {
                return config;
            }
        }
        return null;
    }

    /**
     * Returns the local java launch config type
     */
    protected ILaunchConfigurationType getCactusLaunchConfigType()
    {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        return lm.getLaunchConfigurationType(
            CactusLaunchConfiguration.ID_CACTUS_APPLICATION);
    }
}
