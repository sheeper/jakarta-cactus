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
package org.apache.cactus.framework.internal.orchestrator;

import org.apache.cactus.framework.internal.orchestrator.handlers.GetResultHandler;
import org.apache.cactus.framework.internal.orchestrator.handlers.GetTestHandler;
import org.apache.cactus.framework.internal.orchestrator.handlers.SetResultHandler;
import org.apache.cactus.framework.internal.orchestrator.handlers.SetTestHandler;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;

public class Orchestrator
{
    private int port;

    public Orchestrator(int port)
    {
        this.port = port;
    }

    public int getPort()
    {
        return this.port;
    }
    
    public void start() throws Throwable
    {
        // Setup HTTP server and attach to it handlers to manage
        // the executing test and to manage retrieval of test results

        HttpServer server = new HttpServer();
        SocketListener listener = new SocketListener();
        listener.setPort(getPort());
        server.addListener(listener);

        HttpContext getTestContext = new HttpContext();
        getTestContext.setContextPath("/gettest");
        getTestContext.addHandler(new GetTestHandler());
        server.addContext(getTestContext);

        HttpContext setTestContext = new HttpContext();
        setTestContext.setContextPath("/settest");
        setTestContext.addHandler(new SetTestHandler());
        server.addContext(setTestContext);

        HttpContext getResultContext = new HttpContext();
        getResultContext.setContextPath("/getresult");
        getResultContext.addHandler(new GetResultHandler());
        server.addContext(getResultContext);

        HttpContext setResultContext = new HttpContext();
        setResultContext.setContextPath("/setresult");
        setResultContext.addHandler(new SetResultHandler());
        server.addContext(setResultContext);

        server.start();
    }
}
