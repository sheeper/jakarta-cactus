/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.eclipse.webapp.ui;

import org.apache.cactus.eclipse.webapp.Webapp;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for the web application.
 * It is displayed in project's property pages.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id$
 */
public class WebAppPropertyPage extends PropertyPage
{
    /**
     * The only UI block for this property page. 
     */
    private WebAppConfigurationBlock webAppConfigurationBlock;

    /**
     * The webapp object that is loaded or persisted. 
     */
    private Webapp webapp;

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(
     *     org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite theParent)
    {
        IJavaProject javaProject = JavaCore.create(getProject());
        webapp = new Webapp(javaProject);
        boolean loadedDefaults = webapp.init();
        if (loadedDefaults)
        {
            // Status line indicating we loaded the defaults 
        }
        webAppConfigurationBlock =
            new WebAppConfigurationBlock(
                getShell(),
                javaProject,
                webapp.getOutput(),
                webapp.getDir(),
                webapp.getClasspath());
        return webAppConfigurationBlock.createContents(theParent);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        webapp.setOutput(webAppConfigurationBlock.getOutput());
        webapp.setDir(webAppConfigurationBlock.getWebappDir());
        webapp.setClasspath(webAppConfigurationBlock.getWebappClasspath());
        try
        {
            webapp.persist();
        }
        catch (CoreException e)
        {
            //TODO: update status line
            return false;
        }
        return true;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    public void performDefaults()
    {
        super.performDefaults();
        webapp.loadDefaultValues();
        webAppConfigurationBlock.update(
            webapp.getOutput(),
            webapp.getDir(),
            webapp.getClasspath());
        webAppConfigurationBlock.refresh();
    }

    /**
     * Returns the project on which this property page has been called.
     * @return the current project
     */
    private IProject getProject()
    {
        IAdaptable adaptable = getElement();
        IProject elem = (IProject) adaptable.getAdapter(IProject.class);
        return elem;
    }
}