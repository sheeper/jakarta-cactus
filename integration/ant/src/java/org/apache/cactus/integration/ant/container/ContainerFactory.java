/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
