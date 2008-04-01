/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog.
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
 * @version $Id: CactusPreferencePage.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class CactusPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{
    /**
     * Sets default plugin preferences.
     */
    public CactusPreferencePage()
    {
        super(GRID);
        setPreferenceStore(CactusPlugin.getDefault().getPreferenceStore());
        setDescription(
            CactusMessages.getString("CactusPreferencePage.description"));
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors()
    {
        addField(
            new StringFieldEditor(
                CactusPreferences.CONTEXT_URL_HOST,
                CactusMessages.getString("CactusPreferencePage.label.host"),
                getFieldEditorParent()));
        addField(
            new IntegerFieldEditor(
                CactusPreferences.CONTEXT_URL_PORT,
                CactusMessages.getString("CactusPreferencePage.label.port"),
                getFieldEditorParent()));
        addField(
            new StringFieldEditor(
                CactusPreferences.CONTEXT_URL_PATH,
                CactusMessages.getString("CactusPreferencePage.label.context"),
                getFieldEditorParent()));
        addField(
            new DirectoryFieldEditor(
                CactusPreferences.TEMP_DIR,
                CactusMessages.getString("CactusPreferencePage.label.temp"),
                getFieldEditorParent()));
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench theWorkbench)
    {
        // nothing to do (yet)
    }

}
