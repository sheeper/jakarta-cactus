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
package org.apache.cactus.framework.internal.orchestrator.handlers;

import java.io.IOException;
import java.io.OutputStream;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.ByteArrayISO8859Writer;

public class GetTestHandler extends AbstractHttpHandler
{
    public static String PATH_IN_CONTEXT = "/gettest";

    private SetTestHandler setTesthandler;
    
    public GetTestHandler(SetTestHandler handler)
    {
        this.setTesthandler = handler;
    }
    
    public void handle(String pathInContext, String pathParams, 
        HttpRequest request, HttpResponse response) 
        throws HttpException, IOException
    {
        if (PATH_IN_CONTEXT.equals(pathInContext))
        {
            String name = this.setTesthandler.getCurrentTestName();
            System.err.println("Gettest: name = [" + name + "]");
            
            OutputStream out = response.getOutputStream();
            ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
            writer.write(name);
            writer.flush();
            response.setIntField(HttpFields.__ContentLength, writer.size());
            out.flush();
            request.setHandled(true);
        }
    }
}
