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
package org.apache.cactus.eclipse.war.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.LibrariesWorkbookPage;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Helper class for creating War files.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id$
 */
public class WebAppConfigurationBlock
{
    private StringButtonDialogField outputField;
    private StringDialogField webappDirField;
    private StringButtonDialogField tempDirField;
    private LibrariesWorkbookPage libraryPage;

    private CheckedListDialogField classPathList;
    private IJavaProject javaProject;
    private Shell shell;

    public WebAppConfigurationBlock(
        Shell theShell,
        IJavaProject theJavaProject,
        String theOutput,
        String theDir,
        String theTempDir,
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
        outputField.setLabelText("Output war");
        outputField.setButtonLabel("Browse...");

        webappDirField = new StringDialogField();
        webappDirField.setDialogFieldListener(adapter);
        webappDirField.setLabelText("Webapp directory");

        tempDirField = new StringButtonDialogField(adapter);
        tempDirField.setDialogFieldListener(adapter);
        tempDirField.setLabelText("Temporary directory");
        tempDirField.setButtonLabel("Browse...");

        update(theOutput, theDir, theTempDir, theEntries);
    }

    private class BuildPathAdapter
        implements IStringButtonAdapter, IDialogFieldListener
    {

        // -------- IStringButtonAdapter --------
        public void changeControlPressed(DialogField field)
        {
            buildPathChangeControlPressed(field);
        }

        // ---------- IDialogFieldListener --------
        public void dialogFieldChanged(DialogField field)
        {
            buildPathDialogFieldChanged(field);
        }
    }

    private void buildPathDialogFieldChanged(DialogField field)
    {
    }

    private void buildPathChangeControlPressed(DialogField field)
    {
        if (field == tempDirField)
        {
            File tempDir = chooseTempDir();
            if (tempDir != null)
            {
                tempDirField.setText(tempDir.getAbsolutePath());
            }
        }
        else
        {
            if (field == outputField)
            {
                File output = chooseOutput();
                if (output != null)
                {
                    outputField.setText(output.getAbsolutePath());
                }
            }
        }
    }
    
    private File chooseOutput()
    {
        File output = new File(outputField.getText());
        if (!output.exists()) {
        	output = output.getParentFile();
        }
        String initPath = "";
        if (output != null)
        {
            initPath = output.getPath();
        }
        FileDialog dialog = new FileDialog(shell);
        dialog.setText(PreferencesMessages.getString("War file selection"));
        dialog.setFilterExtensions(new String[] {"*.war"});
        dialog.setFilterPath(initPath);
        String res = dialog.open();
        if (res != null)
        {
            return (new File(res));
        }
        return null;
    }

    private File chooseTempDir()
    {
        File tempDir = new File(tempDirField.getText());
        String initPath = "";
        if (tempDir != null)
        {
            initPath = tempDir.getPath();
        }
        DirectoryDialog dialog = new DirectoryDialog(shell);
        dialog.setText(
            PreferencesMessages.getString("Temp directory selection"));
        dialog.setMessage("Select temp directory:");
        dialog.setFilterPath(initPath);
        String res = dialog.open();
        if (res != null)
        {
            return (new File(res));
        }
        return null;
    }

    public Control createContents(Composite theParent)
    {
        final Composite topComp = new Composite(theParent, SWT.NONE);
        GridLayout topLayout = new GridLayout();

        topComp.setLayout(topLayout);
        topLayout.numColumns = 3;
        topLayout.marginWidth = 0;
        topLayout.marginHeight = 0;
        topComp.setLayout(topLayout);

        outputField.doFillIntoGrid(topComp, 3);
        webappDirField.doFillIntoGrid(topComp, 3);
        tempDirField.doFillIntoGrid(topComp, 3);

        Control libraryPageControl = libraryPage.getControl(topComp);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 3;
        libraryPageControl.setLayoutData(gd);
        libraryPage.init(javaProject);

        return topComp;
    }

    private void doValidation()
    {

        String text = outputField.getText();
        if (text.length() > 0)
        {
            //            File file = new File(text);
            //            if (!file.isFile())
            //            {
            //                status.setError(PreferencesMessages.getString("JavadocPreferencePage.error.notexists")); 
            //            }
        }
    }

    public String getOutput()
    {
        return outputField.getText();
    }

    public String getWebappDir()
    {
        return webappDirField.getText();
    }

    public String getTempDir()
    {
        return tempDirField.getText();
    }

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

    private ArrayList getExistingEntries(IClasspathEntry[] classpathEntries)
    {
        ArrayList newClassPath = new ArrayList();
        for (int i = 0; i < classpathEntries.length; i++)
        {
            IClasspathEntry curr = classpathEntries[i];
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

    public void update(
        String theOutput,
        String theDir,
        String theTempDir,
        IClasspathEntry[] theEntries)
    {
        outputField.setText(theOutput);
        webappDirField.setText(theDir);
        tempDirField.setText(theTempDir);
        classPathList.setElements(getExistingEntries(theEntries));
    }

    public void refresh()
    {
        libraryPage.init(javaProject);
    }
}
