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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Provide convenient methods to read information from a Jar archive.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public class DefaultJarArchive implements JarArchive
{
    // Instance Variables ------------------------------------------------------

    /**
     * The content of the archive as an input stream.
     */
    private byte content[];

    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theFile The archive file
     * @throws IOException If there was a problem reading the WAR
     */
    public DefaultJarArchive(File theFile)
        throws IOException
    {
        this(new FileInputStream(theFile));
    }

    /**
     * Constructor.
     * 
     * @param theInputStream The input stream for the archive (it will be closed
     *        after the constructor returns)
     * @throws IOException If there was a problem reading the WAR
     */
    public DefaultJarArchive(InputStream theInputStream)
        throws IOException
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int bytesRead = -1;
            while ((bytesRead = theInputStream.read(buffer)) != -1)
            {
                baos.write(buffer, 0, bytesRead);
            }
            this.content = baos.toByteArray();
        }
        finally
        {
            if (theInputStream != null)
            {
                theInputStream.close();
            }
        }
    }

    // Public Methods ----------------------------------------------------------

    /**
     * @see JarArchive#containsClass(String)
     */
    public boolean containsClass(String theClassName)
        throws IOException
    {
        String resourceName = theClassName.replace('.', '/') + ".class";
        return (getResource(resourceName) != null);
    }

    /**
     * @see JarArchive#findResource(String)
     */
    public final String findResource(String theName)
        throws IOException
    {
        JarInputStream in = null;
        try
        {
            in = new JarInputStream(getContentAsStream());
            ZipEntry entry = null;
            while ((entry = in.getNextEntry()) != null)
            {
                String entryName = entry.getName();
                int lastSlashIndex = entryName.lastIndexOf('/');
                if (lastSlashIndex >= 0)
                {
                    entryName = entryName.substring(lastSlashIndex + 1);
                }
                if (entryName.equals(theName))
                {
                    return entry.getName();
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        return null;
    }

    /**
     * @see JarArchive#getResource(String)
     */
    public final InputStream getResource(String thePath)
        throws IOException
    {
        JarInputStream in = null;
        try
        {
            in = getContentAsStream();
            ZipEntry zipEntry = null;
            while ((zipEntry = in.getNextEntry()) != null)
            {
                if (thePath.equals(zipEntry.getName()))
                {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte bytes[] = new byte[2048];
                    int bytesRead = -1;
                    while ((bytesRead = in.read(bytes)) != -1)
                    {
                        buffer.write(bytes, 0, bytesRead);
                    }
                    return new ByteArrayInputStream(buffer.toByteArray());
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        return null;
    }

    /**
     * @see JarArchive#getResources(String)
     */
    public final List getResources(String thePath)
        throws IOException
    {
        List resources = new ArrayList();
        JarInputStream in = null;
        try
        {
            in = getContentAsStream();
            ZipEntry zipEntry = null;
            while ((zipEntry = in.getNextEntry()) != null)
            {
                if ((zipEntry.getName().startsWith(thePath)
                 && !zipEntry.getName().equals(thePath)))
                {
                    resources.add(zipEntry.getName());
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        return resources;
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Returns the content of the archive as <code>JarInputStream</code>.
     * 
     * @return The input stream
     * @throws IOException If an exception occurred reading the archive
     */
    protected final JarInputStream getContentAsStream()
        throws IOException
    {
        return new JarInputStream(new ByteArrayInputStream(this.content));
    }
}
