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
package org.apache.cactus.integration.ant;

import org.apache.cactus.integration.api.version.Version;
import org.codehaus.cargo.module.webapp.EjbRef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the nested element cactuswar of the cactifyear task.
 * This element can be configured exactly as the cactifywar task 
 * with some additions as context within the ear file.
 *
 *
 * @version $Id: CactusWar.java 239162 2005-04-26 09:57:59Z grimsell $
 */
public class CactusWar
{
    /**
     * Name of the generated web app file.
     */
    private static final String FILE_NAME = "cactus.war";
    
    /**
     * Context of the cactus web application.
     */
    private String context;
    
    /**
     * The web-app version to use when creating a WAR from scratch.
     */
    private String version = null;
    
    /**
     * List of ejb-refs to add to the deployment descriptor 
     * of the cactified war.
     */
    private List ejbRefs = new ArrayList();
    
    /**
     * The destination file of the cactification process.
     */
    private File destFile = null;
    
    /**
     * @return Returns the context.
     */
    public String getContext()
    {
        return context;
    }
    
    /**
     * @param theContext The context to set.
     */
    public void setContext(String theContext)
    {
        context = theContext;
    }   
    
    /**
     * 
     * @return the name of the web app file
     */
    public String getFileName()
    {
        return FILE_NAME;
    }
    
    /**
     * Sets the web-app version to use when creating a WAR file from scratch.
     * 
     * @param theVersion The version
     */
    public final void setVersion(Version theVersion)
    {
        this.version = theVersion.getValue();
    }
    
    /**
     * Adds a configured EjbRef instance. Called by Ant.
     * 
     * @param theEjbRef the EjbRef to add
     */
    public final void addConfiguredEjbref(EjbRef theEjbRef)
    {
        ejbRefs.add(theEjbRef);
    }
    
    /**
     * Setter method for the destination file.
     * @param theDestFile to set
     */
    public void setDestFile(File theDestFile)
    {
        this.destFile = theDestFile;
    }
}
