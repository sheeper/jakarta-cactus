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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cactus.framework.internal.orchestrator.handlers.GetTestHandler;
import org.apache.cactus.framework.internal.orchestrator.handlers.SetTestHandler;

public class OrchestratorClient
{
    public void setTest(String name)
    	throws MalformedURLException, IOException
    {
	    // TODO: get port from configuration
	    URL url = new URL(
	        "http://localhost:7777" + SetTestHandler.PATH_IN_CONTEXT
	        + "?name=" + name);
	 
	    // TODO: use proper logging system
	    System.err.println("URL = [" + url + "]");
	    HttpURLConnection connection = 
	        (HttpURLConnection) url.openConnection(); 
	    connection.setRequestMethod("GET");
	    connection.getInputStream();
    }

    public String getTest()
		throws MalformedURLException, IOException
    {
	    // TODO: get port from configuration
	    URL url = new URL(
	        "http://localhost:7777" + GetTestHandler.PATH_IN_CONTEXT);
	 
	    // TODO: use proper logging system
	    System.err.println("URL = [" + url + "]");
	    HttpURLConnection connection = 
	        (HttpURLConnection) url.openConnection(); 
	    connection.setRequestMethod("GET");

	    InputStream in = connection.getInputStream();
	    byte[] b = new byte[in.available()];
	    in.read(b);
	    
	    return new String(b);
    }

}
