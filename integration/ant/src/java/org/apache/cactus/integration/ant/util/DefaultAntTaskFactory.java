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
package org.apache.cactus.integration.ant.util;

import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Default {@link AntTaskFactory} for creating Ant tasks.
 *
 * @version $Id$
 */
public class DefaultAntTaskFactory implements AntTaskFactory
{
    /**
     * The current {@link Project} being executed.
     */
    private Project currentProject;
    
    /**
     * The current Ant task being executed.
     */
    private String currentTaskName;
    
    /**
     * The current {@link Location} of the Task being executed.
     */
    private Location currentLocation;
    
    /**
     * The current {@link Target} being executed.
     */
    private Target currentOwningTarget;

    /**
     * @param theProject the current project
     * @param theCurrentTaskName the current Ant task name 
     * @param theCurrentLocation the current task location
     * @param theCurrentTarget the current target being executed 
     */
    public DefaultAntTaskFactory(Project theProject,
        String theCurrentTaskName, Location theCurrentLocation,
        Target theCurrentTarget)
    {
        this.currentProject = theProject;
        this.currentTaskName = theCurrentTaskName;
        this.currentLocation = theCurrentLocation;
        this.currentOwningTarget = theCurrentTarget;
    }

    /**
     * @see AntTaskFactory#createTask(String) 
     */
    public Task createTask(String theName)
    {
        Task retVal = this.currentProject.createTask(theName);
        if (retVal != null)
        {
            retVal.setTaskName(this.currentTaskName);
            retVal.setLocation(this.currentLocation);
            retVal.setOwningTarget(this.currentOwningTarget);
        }
        return retVal;
    }
}
