/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.eclipse.webapp.internal.ui;

import org.apache.cactus.eclipse.webapp.internal.Webapp;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferencePage;

/**
 * Property page for the web application.
 * It is displayed in project's property pages.
 * 
 * @version $Id: WebAppPropertyPage.java 238816 2004-02-29 16:36:46Z vmassol $
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
    protected final Control createContents(final Composite theParent)
    {
        IJavaProject javaProject = JavaCore.create(getProject());
        webapp = new Webapp(javaProject);
        webapp.init();

        // TODO: Status line indicating we loaded the defaults 
//        boolean loadedDefaults = webapp.init();
//        if (loadedDefaults)
//        {
//        }

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
    public final boolean performOk()
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
            System.err.println(e.getStackTrace());
        	  e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    public final void performDefaults()
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
        
        //return getProject();
    }

	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub
		setPreferenceStore(WebappPlugin.getDefault().getPreferenceStore());
		
		
	}
}
