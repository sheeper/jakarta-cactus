/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.cactus.integration.ant.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.FilterChain;

/**
 * Utility class that provides a couple of methods for extracting files stored
 * as resource in a JAR.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class ResourceUtils
{

    // Constructors ------------------------------------------------------------

    /**
     * Private constructor to ensure static usage.
     */
    private ResourceUtils()
    {
    }

    // Public Static Methods ---------------------------------------------------

    /**
     * Copies a container resource from the JAR into the specified file.
     * 
     * @param theProject The Ant project
     * @param theResourceName The name of the resource, relative to the
     *        org.apache.cactus.integration.ant.container package
     * @param theDestFile The file to which the contents of the resource should
     *        be copied
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public static void copyResource(Project theProject, String theResourceName,
        File theDestFile)
        throws IOException
    {
        InputStream in =
            ResourceUtils.class.getResourceAsStream(theResourceName);
        if (in == null)
        {
            throw new IOException("Resource '" + theResourceName
                + "' not found");
        }
        
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(theDestFile);
            
            byte buf[] = new byte[4096];
            int numBytes = 0;
            while ((numBytes = in.read(buf)) > 0)
            {
                out.write(buf, 0, numBytes);
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
    }
    
    /**
     * Copies a container resource from the JAR into the specified file, thereby
     * applying the specified filters.
     * 
     * @param theProject The Ant project
     * @param theResourceName The name of the resource, relative to the
     *        org.apache.cactus.integration.ant.container package
     * @param theDestFile The file to which the contents of the resource should
     *        be copied
     * @param theFilterChain The ordered list of filter readers that should be
     *        applied while copying
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public static void copyResource(Project theProject, String theResourceName,
        File theDestFile, FilterChain theFilterChain)
        throws IOException
    {
        InputStream resource =
            ResourceUtils.class.getResourceAsStream(theResourceName);
        if (resource == null)
        {
            throw new IOException("Resource '" + theResourceName
                + "' not found");
        }
        
        BufferedReader in = null;
        BufferedWriter out = null;
        try
        {
            ChainReaderHelper helper = new ChainReaderHelper();
            helper.setBufferSize(8192);
            helper.setPrimaryReader(new BufferedReader(
                new InputStreamReader(resource)));
            Vector filterChains = new Vector();
            filterChains.add(theFilterChain);
            helper.setFilterChains(filterChains);
            helper.setProject(theProject);
            in = new BufferedReader(helper.getAssembledReader());

            out = new BufferedWriter(new FileWriter(theDestFile));

            String line = null;
            while ((line = in.readLine()) != null)
            {
                if (line.length() == 0)
                {
                    out.newLine();
                }
                else
                {
                    out.write(line);
                    out.newLine();
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
    }
    
    /**
     * Search for the given resource and return the directory or archive
     * that contains it.
     * 
     * <p>Doesn't work for archives in JDK 1.1 as the URL returned by
     * getResource doesn't contain the name of the archive.</p>
     * 
     * @param theResourceName The name of the resource
     * @return The directory or archive containing the specified resource
     */
    public static File getResourceLocation(String theResourceName)
    {
        File file = null;
        URL url = ResourceUtils.class.getResource(theResourceName);
        if (url != null)
        {
            String urlString = url.toString();
            if (urlString.startsWith("jar:file:"))
            {
                int pling = urlString.indexOf("!");
                String jar = urlString.substring(9, pling);
                file = new File(URLDecoder.decode(jar));
            }
            else if (urlString.startsWith("file:"))
            {
                int tail = urlString.indexOf(theResourceName);
                String dir = urlString.substring(5, tail);
                file = new File(URLDecoder.decode(dir));
            }
        }
        return file;
    }

}
