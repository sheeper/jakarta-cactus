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
package org.apache.cactus.eclipse.runner.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page for the containers
 * that is contributed to the Cactus Preferences dialog.
 * <p>
 *   By subclassing <samp>FieldEditorPreferencePage</samp>, we can use the
 *   field support built into JFace that allows us to create a page that is 
 *   small and knows how to save, restore and apply itself.
 * </p>
 * <p>
 *   This page is used to modify preferences only. They are stored in the
 *   preference store that belongs to the main plug-in class. That way,
 *   preferences can be accessed directly via the preference store.
 * </p>
 * 
 * @version $Id: ContainersPreferencePage.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class ContainersPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{
    /**
     * Array of container identifiers.
     */
    private String[] containerIds;
    /**
     * Sets default plugin container preferences.
     */
    public ContainersPreferencePage()
    {
        super(GRID);
        setPreferenceStore(CactusPlugin.getDefault().getPreferenceStore());
        setDescription(
            CactusMessages.getString("ContainersPreferencePage.description"));
    }
    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors()
    {
        Composite parent = getFieldEditorParent();
        for (int i = 0; i < containerIds.length; i++)
        {
            DirectoryFieldEditor dirEditor =
                new DirectoryFieldEditor(
                    containerIds[i],
                    containerIds[i],
                    parent);
            dirEditor.getTextControl(parent).setToolTipText(
                CactusMessages.getString(
                    "ContainersPreferencePage.label.container"));
            addField(dirEditor);
        }
        BooleanFieldEditor jetty =
            new BooleanFieldEditor(
                CactusPreferences.JETTY,
                CactusMessages.getString(
                    "ContainersPreferencePage.label.jetty"),
                BooleanFieldEditor.SEPARATE_LABEL,
                parent);
        jetty.getLabelControl(parent).setToolTipText(
            CactusMessages.getString(
                "ContainersPreferencePage.label.jetty.tooltip"));
        addField(jetty);
        StringFieldEditor jettyXML =
            new StringFieldEditor(
                CactusPreferences.JETTY_XML,
                CactusMessages.getString(
                    "ContainersPreferencePage.label.jettyxml"),
                parent);
        jettyXML.getTextControl(parent).setToolTipText(
            CactusMessages.getString(
                "ContainersPreferencePage.label.jettyxml.tooltip"));
        addField(jettyXML);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench theWorkbench)
    {
        containerIds = CactusPlugin.getContainerIds();

    }

}
