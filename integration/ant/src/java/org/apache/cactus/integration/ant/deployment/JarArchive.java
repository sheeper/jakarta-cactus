/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provide convenient methods to read information from a Jar archive.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public interface JarArchive
{
    /**
     * Returns whether a class of the specified name is contained in the
     * archive.
     * 
     * @param theClassName The name of the class to search for
     * @return Whether the class was found
     * @throws IOException If an I/O error occurred reading the archive
     */
    boolean containsClass(String theClassName) throws IOException;

    /**
     * Returns the full path of a named resource in the archive.
     * 
     * @param theName The name of the resource
     * @return The full path to the resource inside the archive
     * @throws IOException If an I/O error occurred reading the archive
     */
    String findResource(String theName) throws IOException;
    
    /**
     * Returns a resource from the archive as input stream.
     * 
     * @param thePath The path to the resource in the archive
     * @return An input stream containing the specified resource, or
     *         <code>null</code> if the resource was not found in the JAR
     * @throws IOException If an I/O error occurs
     */
    InputStream getResource(String thePath) throws IOException;
    
    /**
     * Returns the list of resources in the specified directory.
     * 
     * @param thePath The directory
     * @return The list of resources
     * @throws IOException If an I/O error occurs
     */
    List getResources(String thePath) throws IOException;
}
