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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Class that encapsulates access to a WAR.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public class DefaultWarArchive extends DefaultJarArchive implements WarArchive
{
    // Instance Variables ------------------------------------------------------

    /**
     * The parsed deployment descriptor.
     */
    private WebXml webXml;

    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theFile The web application archive
     * @throws IOException If there was a problem reading the WAR
     */
    public DefaultWarArchive(File theFile)
        throws IOException
    {
        super(theFile);
    }

    /**
     * Constructor.
     * 
     * @param theInputStream The input stream for the web application archive
     * @throws IOException If there was a problem reading the WAR
     */
    public DefaultWarArchive(InputStream theInputStream)
        throws IOException
    {
        super(theInputStream);
    }

    // Public Methods ----------------------------------------------------------

    /**
     * @see WarArchive#getWebXml()
     */
    public final WebXml getWebXml()
        throws IOException, SAXException, ParserConfigurationException
    {
        if (this.webXml == null)
        {
            InputStream in = null;
            try
            {
                in = getResource("WEB-INF/web.xml");
                if (in != null)
                {
                    this.webXml = WebXmlIo.parseWebXml(in, null);
                }
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
        }
        return this.webXml;
    }

    /**
     * Returns whether a class of the specified name is contained in the web-app
     * archive, either directly in WEB-INF/classes, or in one of the JARs in
     * WEB-INF/lib.
     * 
     * @param theClassName The name of the class to search for
     * @return Whether the class was found in the archive
     * @throws IOException If an I/O error occurred reading the archive
     */
    public final boolean containsClass(String theClassName)
        throws IOException
    {
        // Look in WEB-INF/classes first
        String resourceName =
            "WEB-INF/classes/" + theClassName.replace('.', '/') + ".class";
        if (getResource(resourceName) != null)
        {
            return true;
        }

        // Next scan the JARs in WEB-INF/lib
        List jars = getResources("WEB-INF/lib/");
        for (Iterator i = jars.iterator(); i.hasNext();)
        {
            JarArchive jar = new DefaultJarArchive(
                getResource((String) i.next()));
            if (jar.containsClass(theClassName))
            {
                return true;
            }
        }

        return false;
    }

}
