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
package org.apache.cactus.eclipse.runner.common;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Filter for jar files.
 * i.e. accepts files like 'library.jar'
 * 
 * @version $Id: JarFilenameFilter.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class JarFilenameFilter implements FilenameFilter
{
    /**
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File theDir, String theFilename)
    {
        return theFilename.endsWith(".jar");
    }
}