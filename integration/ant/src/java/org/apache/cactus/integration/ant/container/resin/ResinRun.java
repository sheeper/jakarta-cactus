/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.cactus.integration.ant.container.AbstractServerRun;

/**
 * Starts/stop Resin by setting up a listener socket. Supports Resin 2.0.x,
 * 2.1.x and 3.x.
 *
 * @version $Id$
 */
public class ResinRun extends AbstractServerRun
{
    /**
     * The started Resin server class. We use <code>Object</code> instead of
     * the Resin class so that we don't need the Resin jars in the classpath
     * to compile this class.
     */
    private Object resinServer;

    /**
     * @param theArgs the command line arguments
     */
    public ResinRun(String[] theArgs)
    {
        super(theArgs);
    }

    /**
     * Entry point to start/stop the Resin server.
     *
     * @param theArgs the command line arguments
     */
    public static void main(String[] theArgs)
    {
        ResinRun resin = new ResinRun(theArgs);

        resin.doRun();
    }

    /**
     * Start the Resin server. We use reflection so that the Resin jars do not
     * need to be in the classpath to compile this class.
     * 
     * @see AbstractServerRun#doStartServer
     */
    protected final void doStartServer(String[] theArgs)
    {
        try
        {
            if (isResinVersion("2.0"))
            {
                startResin20x(theArgs);
            }
            else if (isResinVersion("2.1"))
            {
                startResin21x(theArgs);
            }
            else if (isResinVersion("3"))
            {
                startResin3x(theArgs);
            }
            else
            {
                throw new RuntimeException("Unsupported Resin version ["
                    + getResinVersion() + "]");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to start Resin server");
        }
    }

    /**
     * Starts Resin 2.0.x
     *
     * @param theArgs the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void startResin20x(String[] theArgs) throws Exception
    {
        Class resinClass = 
            Class.forName("com.caucho.server.http.ResinServer");
        Constructor constructor = resinClass.getConstructor(
            new Class[] {theArgs.getClass(), boolean.class});

        this.resinServer = constructor.newInstance(
            new Object[] {theArgs, Boolean.TRUE});
    
        Method initMethod = this.resinServer.getClass().getMethod("init", 
            new Class[] {boolean.class});

        initMethod.invoke(this.resinServer, new Object[] {Boolean.TRUE});
    }

    /**
     * Starts Resin 2.1.x
     *
     * @param theArgs the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void startResin21x(String[] theArgs) throws Exception
    {
        Class resinClass = 
            Class.forName("com.caucho.server.http.ResinServer");
        Constructor constructor = resinClass.getConstructor(
            new Class[] {theArgs.getClass(), boolean.class});

        this.resinServer = constructor.newInstance(
            new Object[] {theArgs, Boolean.TRUE});
        
        Method initMethod = this.resinServer.getClass().getMethod("init",
            new Class[] {ArrayList.class});

        initMethod.invoke(this.resinServer, new Object[] {null});
    }

    /**
     * Starts Resin 3.x
     *
     * @param theArgs the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void startResin3x(final String[] theArgs) throws Exception
    {
        // Start the server in another thread so that it doesn't block
        // the current thread. It seems that Resin 3.x is acting differently
        // than Resin 2.x which was not blocking and thus which did not need
        // to be started in a separate thread.
        Thread startThread = new Thread()
        {
            public void run()
            {
                try
                {
                    Class resinClass = 
                        Class.forName("com.caucho.server.http.ResinServer");
                    
                    Method mainMethod = resinClass.getMethod("main", 
                        new Class[] {String[].class});
                                
                    mainMethod.invoke(null, new Object[] {theArgs});
                }
                catch (Exception e)
                {
                    throw new RuntimeException(
                        "Failed to start Resin 3.x. Error = ["
                        + e.getMessage() + "]");
                }
            }
        };
        startThread.start();
    }
    
    /**
     * Stops the Resin server. We use reflection so that the Resin jars do not
     * need to be in the classpath to compile this class.
     * 
     * @see AbstractServerRun#doStopServer
     */
    protected final void doStopServer(String[] theArgs, 
        Thread theRunningServerThread)
    {
        try
        {
            if (isResinVersion("2.0"))
            {
                stopResin20x(theArgs);
            }
            else if (isResinVersion("2.1"))
            {
                stopResin20x(theArgs);
            }
            else if (isResinVersion("3"))
            {
                stopResin3x(theArgs, theRunningServerThread);
            }
            else
            {
                throw new RuntimeException("Unsupported Resin version ["
                    + getResinVersion() + "]");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(
                "Failed to stop the running Resin server");
        }
    }
    
    /**
     * Stops Resin 2.0.x and 2.1.x versions.
     *
     * @param theArgs the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void stopResin20x(String[] theArgs) throws Exception
    {
        Method closeMethod = this.resinServer.getClass().getMethod(
            "close", null);

        closeMethod.invoke(this.resinServer, null);
    }

    /**
     * Stops Resin 3.x.
     *
     * @param theArgs the command line arguments for starting the server
     * @param theRunningServerThread the thread in which the server is running
     * @throws Exception if an error happens when starting the server
     */
    private void stopResin3x(String[] theArgs,
        Thread theRunningServerThread) throws Exception
    {
        // As we don't know how to properly stop a running Resin server,
        // we simply try to kill the thread in which it is running. 
        // Not clean...
        theRunningServerThread.stop();
    }
    
    /**
     * @return the Resin version
     */
    private String getResinVersion()
    {
        String version;
        
        try
        {
            Class versionClass = Class.forName("com.caucho.Version");
            Field versionField = versionClass.getField("VERSION");
            version = (String) versionField.get(null);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot get Resin version. Error = ["
                + e.getMessage() + "]");
        }

        return version;
    }

    /**
     * @param theVersionPrefix the version prefix to test for
     * @return true if the Resin version starts with versionPrefix
     */
    private boolean isResinVersion(String theVersionPrefix)
    {
        return getResinVersion().startsWith(theVersionPrefix);
    }
}
