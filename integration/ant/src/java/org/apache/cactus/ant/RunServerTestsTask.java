/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Task to automate running in-container unit test. It has the following
 * syntax when used in Ant:
 * <code><pre>
 *   &lt;runservertests testURL="&t;url&gt;"
 *          startTarget="&lt;start target name&gt;"
 *          stopTarget="&lt;stop target name&gt;"
 *          testTarget="&lt;test target name&gt;"/>
 * </pre></code>
 * where <code>&lt;url&gt;</code> is the URL that is used by this task to
 * ensure that the server is running. Indeed, the algorithm is as follow :
 * <ul>
 *  <li>Checks if server is running by trying to open an HTTP connection to
 *  the URL,</li>
 *  <li>If it fails, call the start target and loop until the HTTP connection
 *  the URL can be established,</li>
 *  <li>Call the test target. This target is supposed to start the test,
 *  usually by running the junit Ant task,</li>
 *  <li>When the tests are finished, call the stop target to stop the server.
 *  Note: The stop target is called only if the server was not already running
 *  when this task was executed.</li>
 * </ul>
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 * 
 * @deprecated The implementation of this task has moved to
 *             {@link org.apache.cactus.integration.ant.RunServerTestsTask}. 
 */
public class RunServerTestsTask
    extends org.apache.cactus.integration.ant.RunServerTestsTask
{
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        log("The task 'org.apache.cactus.ant.RunServerTestsTask' is "
            + "deprecated. Use "
            + "'org.apache.cactus.integration.ant.RunServerTestsTask' instead",
            Project.MSG_WARN);
        super.execute();
    }

}
