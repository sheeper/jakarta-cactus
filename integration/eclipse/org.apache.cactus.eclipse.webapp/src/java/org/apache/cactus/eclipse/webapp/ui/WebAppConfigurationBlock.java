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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.LibrariesWorkbookPage;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * UI block which shows a list of jar entries.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id$
 */
public class WebAppConfigurationBlock
{
    /**
     * Field for the output war location. 
     */
    private StringButtonDialogField outputField;

    /**
     * Field for the webapp location. 
     */
    private StringDialogField webappDirField;

    /**
     * UI block that shows a list of jar entries. 
     */
    private LibrariesWorkbookPage libraryPage;

    /**
     * List of entries displayed by the libraryPage. 
     */
    private CheckedListDialogField classPathList;

    /**
     * Java project needed for the libraryPage initialization. 
     */
    private IJavaProject javaProject;

    /**
     * The shell used by dialog popups (directory and file choosers). 
     */
    private Shell shell;

    /**
     * Constructor.
     * @param theShell The shell used by dialog popups
     *     (directory and file choosers)
     * @param theJavaProject Java project needed for libraryPage initialization
     * @param theOutput initial output field value
     * @param theDir initial webapp directory value
     * @param theEntries initial list of entries
     */
    public WebAppConfigurationBlock(
        Shell theShell,
        IJavaProject theJavaProject,
        String theOutput,
        String theDir,
        IClasspathEntry[] theEntries)
    {
        shell = theShell;
        javaProject = theJavaProject;
        BuildPathAdapter adapter = new BuildPathAdapter();

        classPathList =
            new CheckedListDialogField(null, null, new LabelProvider());
        classPathList.setDialogFieldListener(adapter);
        IWorkspaceRoot root = JavaPlugin.getWorkspace().getRoot();
        libraryPage = new LibrariesWorkbookPage(root, classPathList);
        outputField = new StringButtonDialogField(adapter);
        outputField.setDialogFieldListener(adapter);
        outputField.setLabelText(
            WebappMessages.getString(
                "WebAppConfigurationBlock.outputfield.label"));
        outputField.setButtonLabel(
            WebappMessages.getString(
                "WebAppConfigurationBlock.outputfield.button.label"));

        webappDirField = new StringDialogField();
        webappDirField.setDialogFieldListener(adapter);
        webappDirField.setLabelText(
            WebappMessages.getString(
                "WebAppConfigurationBlock.webappdirfield.label"));
        update(theOutput, theDir, theEntries);
    }

    /**
     * Adapter that displatches control events.
     */
    private class BuildPathAdapter
        implements IStringButtonAdapter, IDialogFieldListener
    {

        /**
         * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.
         *     IStringButtonAdapter#changeControlPressed(
         *     org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
         */
        public void changeControlPressed(DialogField theField)
        {
            webappChangeControlPressed(theField);
        }

        /**
         * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.
         *     IDialogFieldListener#dialogFieldChanged(
         *     org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
         */
        public void dialogFieldChanged(DialogField theField)
        {
            webappDialogFieldChanged(theField);
        }
    }

    /**
     * Adapter that dispatches events from Dialog fields.
     * Possible use : validation of an entry in a dialog field.
     * @param theField field that triggered an event.
     */
    private void webappDialogFieldChanged(DialogField theField)
    {
        // TODO: validate entries in dialogs
        // Do nothing.
    }

    /**
     * Adapter that dispatches events from StringButtonDialog fields.
     * @param theField field that triggered an event.
     */
    private void webappChangeControlPressed(DialogField theField)
    {
            if (theField == outputField)
            {
                File output = chooseOutput();
                if (output != null)
                {
                    outputField.setText(output.getAbsolutePath());
                }
            }
    }

    /**
     * Displays a file chooser dialog and returns the chosen file.
     * @return File the chosen file
     */
    private File chooseOutput()
    {
        File output = new File(outputField.getText());
        String initPath = "";
        String initFileName = "webapp.war";

        if (output != null)
        {
            if (!output.isDirectory())
            {
                File dir = output.getParentFile();
                if (dir != null)
                {
                    initPath = dir.getPath();
                }
                initFileName = output.getName();
            }
            else
            {
                initPath = output.getPath();
            }
        }
        FileDialog dialog = new FileDialog(shell);
        dialog.setText(
            PreferencesMessages.getString(
                WebappMessages.getString(
                    "WebAppConfigurationBlock.outputchooser.label")));
        dialog.setFileName(initFileName);
        dialog.setFilterExtensions(new String[] {"*.war"});
        dialog.setFilterPath(initPath);
        String res = dialog.open();
        if (res != null)
        {
            return (new File(res));
        }
        return null;
    }


    /**
     * Returns the UI control for this block.
     * @param theParent the parent control.
     * @return Control the created control
     */
    public Control createContents(Composite theParent)
    {
        Composite topComp = new Composite(theParent, SWT.NONE);

        GridLayout topLayout = new GridLayout();
        topLayout.numColumns = 3;
        topLayout.marginWidth = 0;
        topLayout.marginHeight = 0;
        topComp.setLayout(topLayout);

        outputField.doFillIntoGrid(topComp, 3);
        webappDirField.doFillIntoGrid(topComp, 3);

        PixelConverter converter = new PixelConverter(topComp);
        LayoutUtil.setWidthHint(
            outputField.getTextControl(null),
            converter.convertWidthInCharsToPixels(25));
        LayoutUtil.setHorizontalGrabbing(outputField.getTextControl(null));

        Control libraryPageControl = libraryPage.getControl(topComp);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 3;
        libraryPageControl.setLayoutData(gd);
        libraryPage.init(javaProject);
        return topComp;
    }

    /**
     * Returns the text entered in the output field.
     * @return String the text entered
     */
    public String getOutput()
    {
        return outputField.getText();
    }

    /**
     * Returns the text entered in the webapp field.
     * @return String the text entered
     */
    public String getWebappDir()
    {
        return webappDirField.getText();
    }

    /**
     * Returns the array of jar entries selected in the libraryPage.
     * @return IClasspathEntry[] the array of jar entries selected
     */
    public IClasspathEntry[] getWebappClasspath()
    {
        Vector result = new Vector();
        List cplist = classPathList.getElements();
        for (int i = 0; i < cplist.size(); i++)
        {
            CPListElement elem = (CPListElement) cplist.get(i);
            if (elem.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
            {
                result.add(elem.getClasspathEntry());
            }
        }
        return (IClasspathEntry[]) result.toArray(
            new IClasspathEntry[result.size()]);
    }

    /**
     * Returns a list of jar entries contained in an array of entries.
     * @param theClasspathEntries array of classpath entries 
     * @return ArrayList list containing the jar entries
     */
    private ArrayList getExistingEntries(IClasspathEntry[] theClasspathEntries)
    {
        ArrayList newClassPath = new ArrayList();
        for (int i = 0; i < theClasspathEntries.length; i++)
        {
            IClasspathEntry curr = theClasspathEntries[i];
            if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
            {
                try
                {
                    newClassPath.add(
                        CPListElement.createFromExisting(curr, javaProject));
                }
                catch (NullPointerException e)
                {
                    // an error occured when parsing the entry
                    // (possibly invalid entry)
                    // We don't add it
                }
            }
        }
        return newClassPath;

    }

    /**
     * Refreshes the control with the given values.
     * @param theOutput webapp output war location 
     * @param theDir webapp directory
     * @param theEntries jar entries for the webapp
     */
    public void update(
        String theOutput,
        String theDir,
        IClasspathEntry[] theEntries)
    {
        outputField.setText(theOutput);
        webappDirField.setText(theDir);
        classPathList.setElements(getExistingEntries(theEntries));
    }

    /**
     * Refreshes the control.
     */
    public void refresh()
    {
        libraryPage.init(javaProject);
    }
}
