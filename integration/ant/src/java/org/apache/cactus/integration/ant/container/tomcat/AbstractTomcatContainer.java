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
package org.apache.cactus.integration.ant.container.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cactus.integration.ant.container.AbstractJavaContainer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * Base support for Catalina based containers.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 */
public abstract class AbstractTomcatContainer extends AbstractJavaContainer
{

    // Instance Variables ------------------------------------------------------

    /**
     * The Catalina installation directory.
     */
    private File dir;

    /**
     * List of filesets that contain user-specified files that should be added
     * to the Tomcat conf directory.
     */
    private List confFileSets = new ArrayList();

    /**
     * A user-specific server.xml configuration file. If this variable is not
     * set, the default configuration file from the JAR resources will be used.
     */
    private File serverXml;

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    // Public Methods ----------------------------------------------------------

    /**
     * Adds a set of files to include in the Tomcat configuration directory.
     * 
     * @param theConf The fileset to add
     */
    public final void addConf(FileSet theConf)
    {
        theConf.createExclude().setName("**/server.xml");
        this.confFileSets.add(theConf);
    }

    /**
     * Sets the Tomcat installation directory.
     * 
     * @return The directory
     */
    public final File getDir()
    {
        return this.dir;
    }

    /**
     * Sets the Tomcat installation directory.
     * 
     * @param theDir The directory to set
     */
    public final void setDir(File theDir)
    {
        this.dir = theDir;
    }

    /**
     * Sets the server configuration file to use for the test installation of
     * Tomcat.
     * 
     * @return The server.xml file, if set
     */
    public final File getServerXml()
    {
        return this.serverXml;
    }

    /**
     * Sets the server configuration file to use for the test installation of
     * Tomcat.
     *  
     * @param theServerXml The server.xml file
     */
    public final void setServerXml(File theServerXml)
    {
        this.serverXml = theServerXml;
    }

    /**
     * Sets the port to which the container should listen.
     * 
     * @param thePort The port to set
     */
    public final void setPort(int thePort)
    {
        this.port = thePort;
    }

    // AbstractContainer Implementation ----------------------------------------

    /**
     * Returns the port to which the container should listen.
     * 
     * @return The port
     */
    public final int getPort()
    {
        return this.port;
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#init
     */
    public void init()
    {
        if (!this.dir.isDirectory())
        {
            throw new BuildException(this.dir + " is not a directory");
        }
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Copies the configuration files specified by nested &lt;conf&gt; filesets
     * to the conf directory.
     * 
     * @param theConfDir The Tomcat configuration directory
     */
    protected final void copyConfFiles(File theConfDir)
    {
        if (getServerXml() != null)
        {
            FileUtils fileUtils = FileUtils.newFileUtils();
            try
            {
                fileUtils.copyFile(getServerXml(),
                    new File(theConfDir, "server.xml"));
            }
            catch (IOException ioe)
            {
                throw new BuildException("Could not copy " + getServerXml()
                    + " to directory " + theConfDir, ioe);
            }
        }

        if (!this.confFileSets.isEmpty())
        {
            Copy copy = (Copy) createAntTask("copy");
            copy.setTodir(theConfDir);
            for (Iterator i = this.confFileSets.iterator(); i.hasNext();)
            {
                copy.addFileset((FileSet) i.next());
            }
            copy.execute();
        }
    }

}
