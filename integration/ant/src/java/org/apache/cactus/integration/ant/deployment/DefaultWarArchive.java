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
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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
