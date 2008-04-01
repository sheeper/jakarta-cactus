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
package org.apache.cactus.eclipse.runner.containers.ant;

import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;

/**
 * This Ant task is used for running tests between container startup and
 * shutdown.
 *
 * @version $Id: EclipseRunTests.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class EclipseRunTests extends Task implements Runnable
{
    /**
     * Indicates that tests are finished, meaning that the task can terminate.
     */
    private volatile boolean isFinished = false;

    /**
     * Launches the Junit tests in Eclipse
     */
    public void execute()
    {
        Display.getDefault().asyncExec(this);
        while (!isFinished)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // Do nothing
            }
        }
    }

    /**
     * This method notifies the instance that tests are finished and that
     * it can terminate.
     */
    public void finish()
    {
        isFinished = true;
    }

    /**
     * Launches Cactus tests.
     */
    public void run()
    {
        try
        {
            AntContainerManager antManager =
                (AntContainerManager) CactusPlugin.getContainerManager(false);
            antManager.setEclipseRunner(this);
            antManager.preparationDone();
        }
        catch (CoreException e)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString(
                    "CactusLaunch.message.containerManager.error"),
                e.getMessage(), null);
            return;
        }
    }
}
