/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
        // Exclude the server.xml file as there is a "serverXml" specific 
        // property for it.
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
     * @return The server.xml file, if set or null otherwise
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

        if (!getDeployableFile().isWar())
        {
            throw new BuildException("Tomcat doesn't support the "
                + "deployment of EAR files");
        }
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Copies the configuration files specified by nested &lt;conf&gt; filesets
     * to the conf/ directory.
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
