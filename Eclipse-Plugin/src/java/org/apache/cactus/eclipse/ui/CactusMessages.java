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