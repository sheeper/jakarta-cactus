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
 * Starts/stop Resin by setting up a listener socket.
 *
 * @version $Id$
 * @see AbstractServerRun
 * 
 * @deprecated This class has moved to
 *   {@link org.apache.cactus.integration.ant.container.resin.ResinRun}. 
 */
public class ResinRun
    extends org.apache.cactus.integration.ant.container.resin.ResinRun
{

    /**
     * @param theArgs the command line arguments
     */
    public ResinRun(String[] theArgs)
    {
        super(theArgs);
        logDeprecation(System.err, "The class "
            + "'org.apache.cactus.ant.ResinRun' is deprecated. Use "
            + "'org.apache.cactus.integration.ant.container.resin.ResinRun' "
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
