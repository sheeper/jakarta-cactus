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
package org.apache.cactus.eclipse.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;

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
 * @version $Id:$
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 */
public class CactusPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{
    /**
     * Sets default plugin preferences.
     */
    public CactusPreferencePage()
    {
        super(GRID);
        setPreferenceStore(CactusPlugin.getDefault().getPreferenceStore());
        // TODO: externalize
        setDescription("Preferences of the Apache Cactus plug-in "
            + "for in-container unit testing");
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults()
    {
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(CactusPreferences.CONTEXT_URL_SCHEME, "http");
        store.setDefault(CactusPreferences.CONTEXT_URL_HOST, "localhost");
        store.setDefault(CactusPreferences.CONTEXT_URL_PORT, 8080);
        store.setDefault(CactusPreferences.CONTEXT_URL_PATH, "test");
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors()
    {
        // TODO: externalize labels
        addField(new RadioGroupFieldEditor(
            CactusPreferences.CONTEXT_URL_SCHEME, "Protocol:", 1,
            new String[][] { { "HTTP", "http" }, { "HTTP over SSL", "https" } },
            getFieldEditorParent()));
        addField(new StringFieldEditor(
            CactusPreferences.CONTEXT_URL_HOST, "Host:",
            getFieldEditorParent()));
        addField(new IntegerFieldEditor(
            CactusPreferences.CONTEXT_URL_PORT, "Port:",
            getFieldEditorParent()));
        addField(new StringFieldEditor(
            CactusPreferences.CONTEXT_URL_PATH, "Context:",
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
