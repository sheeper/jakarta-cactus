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
package org.apache.cactus.integration.ant.container.enhydra;

import java.lang.reflect.Method;

import org.apache.cactus.integration.ant.container.AbstractServerRun;

/**
 * Starts/stop Enhydra by setting up a listener socket.
 *
 * @author <a href="mailto:digital@ix.net.au">Robert Leftwich</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @see AbstractServerRun
 */
public class EnhydraRun extends AbstractServerRun
{
    /**
     * @param theArgs the command line arguments
     */
    public EnhydraRun(String[] theArgs)
    {
        super(theArgs);
    }

    /**
     * Entry point to start/stop the Enhydra server.
     *
     * @param theArgs the command line arguments
     */
    public static void main(String[] theArgs)
    {
        EnhydraRun enhydra = new EnhydraRun(theArgs);

        enhydra.doRun();
    }

    /**
     * Start the Enhydra server. We use reflection so that the Enhydra jars do
     * not need to be in the classpath to compile this class.
     * 
     * @see AbstractServerRun#doStartServer
     */
    protected final void doStartServer(String[] theArgs)
    {
        try
        {
            Class enhydraClass = 
                Class.forName("com.lutris.multiServer.MultiServer");
            Method initMethod = enhydraClass.getMethod("main", 
                new Class[] {theArgs.getClass()});

            initMethod.invoke(null, new Object[] {theArgs});
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Cannot create instance of MultiServer");
        }
    }

    /**
     * Stops the Enhydra server. We use reflection so that the Enhydra jars do
     * not need to be in the classpath to compile this class.
     * 
     * @see AbstractServerRun#doStopServer
     */
    protected final void doStopServer(String[] theArgs,
        Thread theRunningServerThread) throws Exception
    {
        try
        {
            Class enhydraClass = 
                Class.forName("com.lutris.multiServer.MultiServer");
            Method shutDownMethod = enhydraClass.getMethod("shutdown", null);

            shutDownMethod.invoke(null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Cannot stop running instance of "
                + "MultiServer");
        }
    }
}
