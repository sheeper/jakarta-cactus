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
package org.apache.cactus.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Utiliy methods related to class loading in a webapp environment.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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
