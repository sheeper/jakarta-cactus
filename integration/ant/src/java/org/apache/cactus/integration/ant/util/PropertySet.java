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
package org.apache.cactus.integration.ant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;

/**
 * Ant element used to tell the Cactus task to load a properties file
 * and passed its properties to the client side or server side JVMs. 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class PropertySet
{
    /**
     * Properties file to load.
     */
    private File propertiesFile;

    /**
     * Are the properties for the Cactus server side JVM?
     */
    private boolean isServer;
    
    /**
     * @param thePropertiesFile the properties file to load
     */
    public void setPropertiesFile(File thePropertiesFile)
    {
        this.propertiesFile = thePropertiesFile;
    }

    /**
     * @param isServer if true the properties will be passed to the
     *        Cactus server side JVM
     */
    public void setServer(boolean isServer)
    {
        this.isServer = isServer;
    }

    /**
     * @return true if the properties are to be passed to the Cactus
     *         server side JVM, false otherwise
     */
    public boolean isServer()
    {
        return this.isServer;
    }

    /**
     * @return the properties loaded from the proeprties file
     */
    public ResourceBundle readProperties()
    {
        if (this.propertiesFile == null)
        {
            throw new BuildException("Missing 'propertiesFiles' attribute");
        }
        
        ResourceBundle bundle;
        try
        {
            bundle = new PropertyResourceBundle(
                new FileInputStream(this.propertiesFile));
        } 
        catch (IOException e)
        {
            throw new BuildException("Failed to load properties "
                + "file [" + this.propertiesFile + "]");
        }
        return bundle;
    }
}
