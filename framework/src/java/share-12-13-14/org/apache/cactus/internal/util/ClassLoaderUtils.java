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
package org.apache.cactus.internal.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Utiliy methods related to class loading in a webapp environment.
 *
 * @version $Id$
 */
public class ClassLoaderUtils
{
    /**
     * Try loading a class first by using the context class loader or by using
     * the classloader of the referrer class if the context classloader failed
     * to load the class.
     *
     * @param theClassName the name of the test class
     * @param theReferrer the class will be loaded using the classloader which
     *        has loaded this referrer class
     * @return the class object the test class to call
     * @exception ClassNotFoundException if the class cannot be loaded through
     *            either classloader
     */
    public static Class loadClass(String theClassName, Class theReferrer)
        throws ClassNotFoundException
    {
        // Get the class to call and build an instance of it.
        Class clazz = null;

        try
        {
            // try loading from webapp classloader first
            clazz = loadClassFromWebappClassLoader(theClassName, theReferrer);
        }
        catch (Throwable internalException)
        {
            // Then try first from Context class loader so that we can put the
            // Cactus jar as an external library.
            clazz = loadClassFromContextClassLoader(theClassName);
        }

        return clazz;
    }

    /**
     * Try loading class using the Context class loader.
     *
     * @param theClassName the class to load
     * @return the <code>Class</code> object for the class to load
     * @exception ClassNotFoundException if the class cannot be loaded through
     *            this class loader
     */
    public static Class loadClassFromContextClassLoader(String theClassName)
        throws ClassNotFoundException
    {
        return Class.forName(theClassName, true, 
            Thread.currentThread().getContextClassLoader());
    }

    /**
     * Try loading class using the Webapp class loader.
     *
     * @param theClassName the class to load
     * @param theReferrer the class will be loaded using the classloader which
     *        has loaded this referrer class
     * @return the <code>Class</code> object for the class to load
     * @exception ClassNotFoundException if the class cannot be loaded through
     *            this class loader
     */
    public static Class loadClassFromWebappClassLoader(String theClassName, 
        Class theReferrer) throws ClassNotFoundException
    {
        return Class.forName(theClassName, true, theReferrer.getClassLoader());
    }

    /**
     * Try loading a resource bundle from either the context class loader or
     * the
     *
     * @param theName the resource bundle name
     * @param theReferrer the resource bundle will be loaded using the
     *        classloader which has loaded this referrer class
     * @return the loaded resource bundle
     */
    public static ResourceBundle loadPropertyResourceBundle(String theName, 
        Class theReferrer)
    {
        ResourceBundle bundle;

        try
        {
            // Try to load from the referrer class loader first
            
            // Some JDK implementation will return "null" when calling
            // getClassLoader(), signalling that the classloader is the
            // bootstrap class loader. However, getBundle() does not support
            // passing null for the class loader, hence the following test.
            if (theReferrer.getClassLoader() == null)
            {
                bundle = PropertyResourceBundle.getBundle(theName,
                    Locale.getDefault());
            }
            else
            {                       
                bundle = PropertyResourceBundle.getBundle(theName,
                    Locale.getDefault(), theReferrer.getClassLoader());
            }
        }
        catch (MissingResourceException e)
        {
            // Then, try to load from context classloader
            bundle = PropertyResourceBundle.getBundle(theName, 
                Locale.getDefault(), 
                Thread.currentThread().getContextClassLoader());
        }

        return bundle;
    }
}
