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
package org.apache.cactus.maven2.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Dependency that we use for the lib folder add-on's.
 * 
 * @version $Id: Dependency.java 238816 2004-02-29 16:36:46Z ptahchiev$
 */
public class Dependency 
extends org.codehaus.cargo.maven2.configuration.Dependency 
{
    /**
     * We override this mothod. 
     * @param theProject The Maven project parameter
     * @param theLog The Maven Log parameter
     * @return the path to the dependency as <code>java.lang.String</code>
     * @throws MojoExecutionException in case an error occurs
     */
    public String getDependencyPath(MavenProject theProject, Log theLog) 
    throws MojoExecutionException
    {
        String path = getLocation();

        if (path == null)
        {
            if ((getGroupId() == null) || (getArtifactId() == null))
            {
                throw new MojoExecutionException("You must specify a "
                    + "groupId/artifactId or a location that points to a "
                    + "directory or JAR");
            }

            // Default to jar if not type is specified
            if (getType() == null)
            {
                setType("jar");
            }

            path = findArtifactLocation(theProject.getArtifacts(), theLog);
        }

        theLog.debug("Classpath location = [" + new File(path).getPath() + "]");

        return new File(path).getPath();
    }
}
