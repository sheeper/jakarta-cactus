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
package org.apache.cactus.integration.ant.container;

import java.io.File;

import org.apache.cactus.integration.ant.deployment.WarArchive;

/**
 * Logic common to all deployable implementations (WAR and EAR
 * deployments).  
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public abstract class AbstractDeployableFile 
    implements DeployableFile, Cloneable
{
    /**
     * The WAR or EAR file to deploy.
     */
    protected File deployableFile;

    /**
     * WAR deployment descriptor as a java object. In case of an EAR,
     * it is the first WAR containing the Cactus Servlet redirector.
     */
    protected WarArchive warArchive;

    /**
     * Webapp context path containing the Cactus tests
     */
    protected String testContext;
        
    /**
     * Servlet mapping of the Cactus Servlet redirector found
     * in the warArchive WAR.
     */
    protected String servletRedirectorMapping;

    /**
     * Filter mapping of the Cactus Filter redirector found
     * in the warArchive WAR.
     */
    protected String filterRedirectorMapping;

    /**
     * JSP mapping of the Cactus JSP redirector found
     * in the warArchive WAR.
     */
    protected String jspRedirectorMapping;

    /**
     * @see DeployableFile#getFile()
     */
    public final File getFile()
    {
        return this.deployableFile;
    }

    /**
     * @param theDeployableFile the file to deploy
     */
    public final void setFile(File theDeployableFile)
    {
        this.deployableFile = theDeployableFile;
    }
    
    /**
     * @see DeployableFile#getTestContext()
     */
    public final String getTestContext()
    {
        return this.testContext;
    }

    /**
     * @see DeployableFile#setTestContext(String)
     */
    public final void setTestContext(String theTestContext)
    {
        this.testContext = theTestContext;
    }
    
    /**
     * @see DeployableFile#getServletRedirectorMapping()
     */
    public final String getServletRedirectorMapping()
    {
        return this.servletRedirectorMapping;
    }

    /**
     * @param theMapping the servlet redirector mapping
     */
    public final void setServletRedirectorMapping(String theMapping)
    {
        this.servletRedirectorMapping = theMapping;
    }
    
    /**
     * @see DeployableFile#getFilterRedirectorMapping()
     */
    public final String getFilterRedirectorMapping()
    {
        return this.filterRedirectorMapping;
    }

    /**
     * @param theMapping the filter redirector mapping
     */
    public final void setFilterRedirectorMapping(String theMapping)
    {
        this.filterRedirectorMapping = theMapping;
    }

    /**
     * @see DeployableFile#getJspRedirectorMapping()
     */
    public final String getJspRedirectorMapping()
    {
        return this.jspRedirectorMapping;
    }

    /**
     * @param theMapping the JSP redirector mapping
     */
    public final void setJspRedirectorMapping(String theMapping)
    {
        this.jspRedirectorMapping = theMapping;
    }

    /**
     * @see DeployableFile#getWarArchive()
     */
    public final WarArchive getWarArchive()
    {
        return this.warArchive;
    }

    /**
     * @see DeployableFile#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        AbstractDeployableFile file = (AbstractDeployableFile) super.clone();
        file.deployableFile = this.deployableFile;
        file.warArchive = this.warArchive;
        file.testContext = this.testContext;
        file.servletRedirectorMapping = this.servletRedirectorMapping;
        file.filterRedirectorMapping = this.filterRedirectorMapping;
        file.jspRedirectorMapping = this.jspRedirectorMapping;
        return file;
    }
    
    /**
     * @param theWarArchive the WAR archive object
     */
    public final void setWarArchive(WarArchive theWarArchive)
    {
        this.warArchive = theWarArchive;
    }

}
