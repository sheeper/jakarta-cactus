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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class to format text messages from the Cactus property resource 
 * bundle.
 * 
 * @version $Id: WebappMessages.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public final class WebappMessages
{
    /**
     * Name and location of property resource bundle on disk.
     */
    private static final String BUNDLE_NAME =
        "org.apache.cactus.eclipse.webapp.internal.ui.WebappMessages";

    /**
     * The resource bundle object were Cactus messages are stored.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = 
        ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Prevent this class from being instantiated. It contains only static
     * methods.
     */
    private WebappMessages()
    {
    }

    /**
     * Gets a string from the resource bundle and formats it with one argument.
     * 
     * @param theKey the string used to get the bundle value, must not be null
     * @param theArg the object to use when constructing the message
     * @return the formatted string
     */
    public static String getFormattedString(final String theKey, 
        final Object theArg)
    {
        return MessageFormat.format(getString(theKey), 
            new Object[] {theArg});
    }

    /**
     * Gets a string from the resource bundle and formats it with arguments.
     * 
     * @param theKey the string used to get the bundle value, must not be null
     * @param theArgs the objects to use when constructing the message
     * @return the formatted string
     */
    public static String getFormattedString(final String theKey, 
        final Object[] theArgs)
    {
        return MessageFormat.format(getString(theKey), theArgs);
    }

    /**
     * Gets an unformatted string from the resource bundle.
     * 
     * @param theKey the string used to get the bundle value, must not be null
     * @return the string from the resource bundle or "![key name]!" if the key
     *         does not exist in the resource bundle
     */
    public static String getString(final String theKey)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(theKey);
        } 
        catch (MissingResourceException e)
        {
            return '!' + theKey + '!';
        }
    }
}
