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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;

/**
 * Special container support for the Apache Tomcat 4.x servlet container.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * 
 * @version $Id$
 */
public class Tomcat4xContainer extends AbstractCatalinaContainer
{
    // Instance Variables ------------------------------------------------------

    /**
     * A user-specific context xml configuration file.
     */
    private File contextXml;

    // Public Methods ----------------------------------------------------------

    /**
     * @return The context xml file, if set or null otherwise
     */
    public final File getContextXml()
    {
        return this.contextXml;
    }

    /**
     * Sets a user-custom context xml configuration file to use for the test
     * installation of Tomcat.
     * 
     * @param theContextXml the custom context xml file to use
     */
    public final void setContextXml(File theContextXml)
    {
        this.contextXml = theContextXml;
    }
    
    // Container Implementation ------------------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.container.Container#init
     */
    public final void init()
    {
        super.init();

        if (!getVersion().startsWith("4"))
        {
            throw new BuildException(
                "This element doesn't support version " + getVersion()
                + " of Tomcat");
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#startUp
     */
    public final void startUp()
    {
        try
        {
            prepare("tomcat4x", "cactus/tomcat4x");
            invokeBootstrap("start");
        }
        catch (IOException ioe)
        {
            getLog().error("Failed to startup the container", ioe);
            throw new BuildException(ioe);
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#shutDown
     */
    public final void shutDown()
    {
        invokeBootstrap("stop");
    }

    /**
     * Tomcat 4.x-specific setup of a temporary installation of the container.
     * 
     * @param theResourcePrefix The prefix to use when looking up container
     *        resource in the JAR
     * @param theDirName The name of the temporary container installation
     *        directory
     * @throws IOException If an I/O error occurs
     * @see AbstractCatalinaContainer#prepare(String, String)
     */
    protected void prepare(String theResourcePrefix, String theDirName)
        throws IOException
    {
        super.prepare(theResourcePrefix, theDirName);

        FileUtils fileUtils = FileUtils.newFileUtils();
        
        // Copy user-provided context xml file into the temporary webapp/
        // container directory
        File webappsDir = new File(getTmpDir(), "webapps");
        if (getContextXml() != null)
        {
            fileUtils.copyFile(getContextXml(), 
                new File(webappsDir, getContextXml().getName()));
        }
        
    }
    
}
