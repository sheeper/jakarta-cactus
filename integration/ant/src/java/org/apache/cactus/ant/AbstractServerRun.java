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

import java.io.PrintStream;

/**
 * Abstract class for starting/stopping an application server. When this
 * application is first called to start the server, a listener socket is
 * set up. Then, we it is later called to stop the server, we connect to the
 * listener socket and tell the server to stop.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:digital@ix.net.au">Robert Leftwich</a>
 *
 * @version $Id$
 *
 * @deprecated This class has moved to
 *   {@link org.apache.cactus.integration.ant.container.AbstractServerRun}. 
 */
public abstract class AbstractServerRun
    extends org.apache.cactus.integration.ant.container.AbstractServerRun
{
    /**
     * @param theArgs the command line arguments
     */
    public AbstractServerRun(String[] theArgs)
    {
        super(theArgs);
        logDeprecation(System.err, "The class "
            + "'org.apache.cactus.ant.AbstractServerRun' is deprecated. Use "
            + "'org.apache.cactus.integration.ant.container.AbstractServerRun' "
            + "instead");
    }

    /**
     * Prints the specified deprecation message to the output stream.
     * 
     * @param theOut The output stream
     * @param theMessage The deprecation message
     */
    private void logDeprecation(PrintStream theOut, String theMessage)
    {
        theOut.println(theMessage);
    }

}
