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
package org.apache.cactus.eclipse.ui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class to format text messages from the Cactus property resource 
 * bundle.
 * 
 * @version $Id: $
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 */
public class CactusMessages
{
    /**
     * Name and location of property resource bundle on disk.
     */
    private static final String BUNDLE_NAME = 
        "org.apache.cactus.eclipse.ui.CactusMessages";

    /**
     * The resource bundle object were Cactus messages are stored.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = 
        ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Prevent this class from being instantiated. It containes only static
     * methods.
     */
    private CactusMessages()
    {
    }

    /**
     * Gets a string from the resource bundle and formats it with one argument.
     * 
     * @param theKey the string used to get the bundle value, must not be null
     * @param theArg the object to use when constructing the message
     * @return the formatted string
     */
    public static String getFormattedString(String theKey, Object theArg)
    {
        return MessageFormat.format(getString(theKey), 
            new Object[] { theArg });
    }

    /**
     * Gets a string from the resource bundle and formats it with arguments.
     * 
     * @param theKey the string used to get the bundle value, must not be null
     * @param theArgs the objects to use when constructing the message
     * @return the formatted string
     */
    public static String getFormattedString(String theKey, Object[] theArgs)
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
    public static String getString(String theKey)
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