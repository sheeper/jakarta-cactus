/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.integration.ant.container;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;

/**
 * Factory for container support classes.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 */
public class ContainerFactory
{

    // Constants ---------------------------------------------------------------

    /**
     * Name of the resource bundle that contains the definitions of the
     * default containers supported by this element.
     */
    private static final String DEFAULT_CONTAINERS_BUNDLE =
        "org.apache.cactus.integration.ant.container.default";

    // Instance Variables ------------------------------------------------------

    /**
     * The list of nested container elements.
     */
    private ResourceBundle defaultContainers;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     */
    public ContainerFactory()
    {
        defaultContainers =
            PropertyResourceBundle.getBundle(DEFAULT_CONTAINERS_BUNDLE);
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Creates and returns the implementation of the <code>Container</code> 
     * interface which is mapped to the specified name.
     * 
     * @param theName The name of the container
     * @return The instantiated container
     * @throws BuildException If there was a problem creating the container
     */
    public final Container createContainer(String theName) throws BuildException
    {
        Container container = null;
        try
        {
            String className = defaultContainers.getString(theName);
            if (className == null)
            {
                throw unsupportedContainer(theName, null);
            }
            Class clazz = Class.forName(className);
            container = (Container) clazz.newInstance();
        }
        catch (MissingResourceException mre)
        {
            throw unsupportedContainer(theName, mre);
        }
        catch (ClassCastException cce)
        {
            throw unsupportedContainer(theName, cce);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw unsupportedContainer(theName, cnfe);
        }
        catch (InstantiationException ie)
        {
            throw unsupportedContainer(theName, ie);
        }
        catch (IllegalAccessException iae)
        {
            throw unsupportedContainer(theName, iae);
        }
        return container;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Creates an exception that indicates that a specific container is not 
     * supported. 
     * 
     * @param theName The container name
     * @param theCause The root cause of the exception
     * @return The created exception
     */
    private BuildException unsupportedContainer(String theName,
        Exception theCause)
    {
        if (theCause != null)
        {
            return new BuildException("The container '" + theName 
                + "' is not supported", theCause);
        }
        else
        {
            return new BuildException("The container '" + theName 
                + "' is not supported");
        }
    }

}
