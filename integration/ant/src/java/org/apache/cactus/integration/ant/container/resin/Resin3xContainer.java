/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container.resin;

import java.io.File;
import java.io.IOException;

import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;

/**
 * Special container support for the Caucho Resin 3.x servlet container.
 * 
 * @version $Id$
 */
public class Resin3xContainer extends AbstractResinContainer
{
    // AbstractContainer Implementation ----------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.container.Container#getName
     */
    public final String getName()
    {
        return "Resin 3.x";
    }
    
    // AbstractResinContainer Implementation -----------------------------------

    /**
     * @see AbstractResinContainer#getContainerDirName
     */
    protected final String getContainerDirName()
    {
        return "resin3x";
    }

    /**
     * @see AbstractResinContainer#startUpAdditions(Java)
     */
    protected void startUpAdditions(Java theJavaContainer, Path theClasspath)
    {
        // It seems Resin 3.x requires the following property to be 
        // set in order to start...
        theJavaContainer.addSysproperty(createSysProperty(
            "java.util.logging.manager", "com.caucho.log.LogManagerImpl"));

        // Add the resin_home/bin directory to the library path so that the 
        // resin dll/so can be loaded.
        theJavaContainer.addSysproperty(createSysProperty(
            "java.library.path", new File(getDir(), "bin")));

        // Add the tools.jar to the classpath. This is not required for
        // Resin 2.x but it is for Resin 3.x
        addToolsJarToClasspath(theClasspath);          
    }

    /**
     * @see AbstractResinContainer#prepareAdditions(File, FilterChain)
     */
    protected void prepareAdditions(File theInstallDir, 
        FilterChain theFilterChain) throws IOException
    {
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + getContainerDirName() + "/app-default.xml",
            new File(theInstallDir, "app-default.xml"), theFilterChain);
    }
}
