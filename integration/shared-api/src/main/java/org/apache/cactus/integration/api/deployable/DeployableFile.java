/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.integration.api.deployable;

import java.io.File;

import org.codehaus.cargo.module.webapp.WarArchive;


/**
 * Represents a component to deploy in a container. It can either be
 * a WAR or an EAR file. 
 * 
 * @version $Id: DeployableFile.java 239003 2004-05-31 20:05:27Z vmassol $
 */
public interface DeployableFile
{
    /**
     * @return the file to deploy in a container (either WAR or EAR)
     */
    File getFile();

    /**
     * Returns whether the deployable file is a web-app archive (WAR).
     * 
     * @return <code>true</code> if the deployable file is a WAR
     */
    boolean isWar();

    /**
     * Returns whether the deployable file is an enterprise application archive
     * (EAR).
     * 
     * @return <code>true</code> if the deployable file is a EAR
     */
    boolean isEar();

    /**
     * @return the WAR deployment descriptor object for the WAR containing
     *         the Cactus Servlet redirector
     */
    WarArchive getWarArchive();
    
    /**
     * @return the webapp context which holds the Cactus tests
     */
    String getTestContext();

    /**
     * @param theTestContext the test context that will be used to test if the
     *        container is started or not
     */
    void setTestContext(String theTestContext);
    
    /**
     * Returns the first URL-pattern to which the Cactus servlet redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return The mapping, or <code>null</code> if the servlet redirector is
     *         not defined or mapped in the descriptor
     */
    String getServletRedirectorMapping();

    /**
     * Returns the first URL-pattern to which the Cactus filter redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return The mapping, or <code>null</code> if the filter redirector is
     *         not defined or mapped in the descriptor
     */
    String getFilterRedirectorMapping();

    /**
     * Returns the first URL-pattern to which the Cactus JSP redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return The mapping, or <code>null</code> if the JSP redirector is
     *         not defined or mapped in the descriptor
     */
    String getJspRedirectorMapping();

    /**
     * Clone the object.
     * @return the object clone
     * @throws CloneNotSupportedException If clone is not supported (duh)
     */
    Object clone() throws CloneNotSupportedException;
}
