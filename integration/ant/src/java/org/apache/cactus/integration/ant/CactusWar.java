/* 
 * ========================================================================
 * 
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant;

/**
 * Implements the nested element cactuswar of the cactifyear task.
 * This element can be configured exactly as the cactifywar task 
 * with som additions as context within the ear file.
 *
 *
 * @version $Id$
 */
public class CactusWar extends CactifyWarTask
{
    /**
     * Name of the generated web app file
     */
    private static final String FILE_NAME = "cactus.war";
    
    /**
     * Context of the cactus web application
     */
    private String context;
    
    /**
     * @return Returns the context.
     */
    public String getContext()
    {
        return context;
    }
    
    /**
     * @param theContext The context to set.
     */
    public void setContext(String theContext)
    {
        context = theContext;
    }   
    
    /**
     * 
     * @return the name of the web app file
     */
    public String getFileName()
    {
        return FILE_NAME;
    }
}
