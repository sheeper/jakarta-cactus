/*
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.cactus.ant;

import java.util.*;

import org.apache.tools.ant.*;

/**
 * Compute a string (returned as an Ant property) that contains a list of
 * args (in the format [-Dname=value]*) that can be used on a java command line.
 *
 * Example :<br>
 * <pre><code>
 * <property name="property1" value="value1"/>
 * <property name="property3" value="value3"/>
 *
 * <argList property="result">
 *   <property name="property1"/>
 *   <property name="property2"/>
 *   <property name="property3"/>
  * </argList>
 *
 * <echo message="${result}"/>
 * </code></pre>
 * <br>
 * will print "<code>-Dproperty1=value1 -Dproperty3=value3</code>".
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ArgListTask extends Task
{
    /**
     * List of Ant properties to check for inclusing in the arg list
     */
    private Vector properties = new Vector();

    /**
     * Name of Ant property that will be set and which will contain the arg
     * list
     */
    private String newProperty;

    /**
     * Add a new property to the list of properties to check
     */
    public void addProperty(ArgListProperty theProperty)
    {
        this.properties.addElement(theProperty);
    }

    /**
     * Set the name of the new Ant property that will contain the arg list.
     */
    public void setProperty(String theNewProperty)
    {
        this.newProperty = theNewProperty;
    }

    /**
     * Execute task. Check all specified Ant properties for existence and if
     * they exist add them to the arg list ("-Dname=value" format).
     */
    public void execute() throws BuildException
    {
        StringBuffer argBuffer = new StringBuffer();
        boolean isEmpty = true;

        // Build the arg list ("-D" separated string).
        Enumeration args = this.properties.elements();
        while (args.hasMoreElements()) {

            String propertyName =
                ((ArgListProperty)args.nextElement()).getName();

            // Check if this property is defined
            String value = getProject().getProperty(propertyName);
            if (value == null) {
                value = getProject().getUserProperty(propertyName);
                if (value == null) {
                    continue;
                }
            }

            // Yes, add the property the list of args
            if (isEmpty) {
                argBuffer.append("-D" + propertyName + "=" + value);
                isEmpty = false;
            } else {
                argBuffer.append(" -D" + propertyName + "=" + value);
            }
        }

        // Set the new property
        getProject().setProperty(this.newProperty, argBuffer.toString());
    }

}
