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
package org.apache.cactus.ant;

import java.io.File;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Check the existence of a list of properties, display their values and stop 
 * Ant if a property does not exist.
 *
 * Example :<br>
 * <pre><code>
 * <property name="property1" value="value1"/>
 *
 * <checkProperties>
 *   <property name="property1" isfile="true|false"/>
 *   <property name="property2" isfile="true|false"/>
 * </checkProperties>
 * </code></pre>
 * <br>
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class CheckPropertiesTask extends Task
{
    /**
     * List of Ant properties to check.
     */
    private Vector properties = new Vector();

    /**
     * Add a new property to the list of properties to check.
     *
     * @param theProperty the property to add to the list
     */
    public void addProperty(CheckPropertyItem theProperty)
    {
        this.properties.addElement(theProperty);
    }

    /**
     * Execute task.
     *
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        Enumeration properties = this.properties.elements();

        while (properties.hasMoreElements())
        {
            CheckPropertyItem property = 
                (CheckPropertyItem) properties.nextElement();

            String value = getProject().getProperty(property.getName());

            if (value == null)
            {
                // The property does not exist
                throw new BuildException("The property [" + property.getName()
                    + "] is not defined");
            }


            // Print the property name/value
            log(property.getName() + " = [" + value + "]");

            // Check if the file/dir exist
            if (property.isFile())
            {
                File file = project.resolveFile(value);

                if (!file.exists())
                {
                    if (file.isDirectory())
                    {
                        throw new BuildException("The directory [" + value
                            + "] pointed by [" + property.getName() 
                            + "] does not exist");
                    }
                    else
                    {
                        throw new BuildException("The file [" + value
                            + "] pointed by [" + property.getName()
                            + "] does not exist");
                    }
                }
            }
        }
    }
}