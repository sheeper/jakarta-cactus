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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.cactus.integration.ant.deployment.WebXml;
import org.apache.cactus.integration.ant.deployment.WebXmlIo;
import org.apache.cactus.integration.ant.deployment.WebXmlMerger;
import org.apache.cactus.integration.ant.deployment.WebXmlVersion;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.xml.sax.SAXException;

/**
 * An Ant task that injects elements necessary to run Cactus tests into an
 * existing WAR file.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class CactifyWarTask extends War
{

    // Constants ---------------------------------------------------------------

    /**
     * The name of the Cactus filter redirector class.
     */
    private static final String FILTER_REDIRECTOR_CLASS =
        "org.apache.cactus.server.FilterTestRedirector";

    /**
     * The default mapping of the Cactus filter redirector.
     */
    private static final String DEFAULT_FILTER_REDIRECTOR_MAPPING =
        "/FilterRedirector";

    /**
     * The default mapping of the Cactus JSP redirector.
     */
    private static final String DEFAULT_JSP_REDIRECTOR_MAPPING =
        "/JspRedirector";

    /**
     * The name of the Cactus servlet redirector class.
     */
    private static final String SERVLET_REDIRECTOR_CLASS =
        "org.apache.cactus.server.ServletTestRedirector";

    /**
     * The default mapping of the Cactus servlet redirector.
     */
    private static final String DEFAULT_SERVLET_REDIRECTOR_MAPPING =
        "/ServletRedirector";

    // Inner Classes -----------------------------------------------------------

    /**
     * Class for nested <code>&lt;redirector&gt;</code> elements. 
     */
    public static class Redirector
    {

        // Instance Variables --------------------------------------------------

        /**
         * The URL pattern that the redirector will be mapped to. 
         */
        private String mapping;
        
        // Public Methods ------------------------------------------------------

        /**
         * Returns the URL pattern the redirector will be mapped to.
         * 
         * @return The URL pattern
         */
        public String getMapping()
        {
            return this.mapping;
        }

        /**
         * Sets the URL pattern that the redirector should be mapped to.
         * 
         * @param theMapping The URL pattern to set
         */
        public void setMapping(String theMapping)
        {
            this.mapping = theMapping;
        }

    }

    /**
     * Enumeration for the <em>version</em> attribute.
     */
    public static class Version extends EnumeratedAttribute
    {

        /**
         * @see org.apache.tools.ant.types.EnumeratedAttribute#getValues()
         */
        public String[] getValues()
        {
            return new String[] {"2.2", "2.3"};
        }

    }

    // Instance Variables ------------------------------------------------------

    /**
     * The archive that contains the web-app that should be cactified.
     */
    private File srcFile;

    /**
     * Location of the descriptor of which the content should be merged into 
     * the descriptor of the cactified archive.
     */
    private File mergeWebXml;

    /**
     * The Cactus filter redirector.
     */
    private Redirector filterRedirector;

    /**
     * The Cactus JSP redirector.
     */
    private Redirector jspRedirector;

    /**
     * The Cactus servlet redirector.
     */
    private Redirector servletRedirector;

    /**
     * For resolving entities such as DTDs.
     */
    private XMLCatalog xmlCatalog = null;

    /**
     * The web-app version to use when creating a WAR from scratch.
     */
    private String version = null;

    // Public Methods ----------------------------------------------------------

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        WebXml webXml = null;
        if (this.srcFile != null)
        {
            // Add everything that's in the source WAR to the destination WAR
            ZipFileSet currentFiles = new ZipFileSet();
            currentFiles.setSrc(this.srcFile);
            currentFiles.createExclude().setName("WEB-INF/web.xml");
            addZipfileset(currentFiles);

            // Parse the original deployment descriptor
            webXml = getOriginalWebXml();
        }
        else
        {
            if (this.version == null)
            {
                throw new BuildException("You need to specify either the "
                    + "[srcfile] or the [version] attribute");
            }
            WebXmlVersion webXmlVersion = null;
            if (this.version.equals("2.2"))
            {
                webXmlVersion = WebXmlVersion.V2_2;
            }
            else
            {
                webXmlVersion = WebXmlVersion.V2_3;
            }
            try
            {
                webXml = WebXmlIo.newWebXml(webXmlVersion);
            }
            catch (ParserConfigurationException pce)
            {
                throw new BuildException(
                    "Could not create deployment descriptor", pce);
            }
        }

        File tmpWebXml = cactifyWebXml(webXml);
        setWebxml(tmpWebXml);

        addCactusJars();

        try
        {
            super.execute();
        }
        finally
        {
            // Even though the temporary descriptor will get deleted
            // automatically when the VM exits, delete it explicitly here just
            // to be a better citizen
            tmpWebXml.delete();
        }
    }

    /**
     * Adds the Cactus filter test redirector (only one permitted).
     * 
     * @param theFilterRedirector The redirector to add
     */
    public void addFilterRedirector(Redirector theFilterRedirector)
    {
        this.filterRedirector = theFilterRedirector;
    }

    /**
     * Adds the Cactus JSP test redirector (only one permitted).
     * 
     * @param theJspRedirector The redirector to add
     */
    public void addJspRedirector(Redirector theJspRedirector)
    {
        this.jspRedirector = theJspRedirector;
    }

    /**
     * Adds the Cactus servlet test redirector (only one permitted).
     * 
     * @param theServletRedirector The redirector to add
     */
    public void addServletRedirector(Redirector theServletRedirector)
    {
        this.servletRedirector = theServletRedirector;
    }

    /**
     * Adds an XML catalog to the internal catalog.
     *
     * @param theXmlCatalog the XMLCatalog instance to use to look up DTDs
     */
    public final void addConfiguredXMLCatalog(XMLCatalog theXmlCatalog)
    {
        if (this.xmlCatalog == null)
        {
            this.xmlCatalog = new XMLCatalog();
            this.xmlCatalog.setProject(getProject());
        }
        this.xmlCatalog.addConfiguredXMLCatalog(theXmlCatalog);
    }

    /**
     * The descriptor to merge into the original file.
     * 
     * @param theMergeFile the <code>web.xml</code> to merge
     */
    public final void setMergeWebXml(File theMergeFile)
    {
        this.mergeWebXml = theMergeFile;
    }

    /**
     * Sets the web application archive that should be cactified.
     * 
     * @param theSrcFile The WAR file to set  
     */
    public final void setSrcFile(File theSrcFile)
    {
        this.srcFile = theSrcFile;
    }

    /**
     * Sets the web-app version to use when creating a WAR file from scratch.
     * 
     * @param theVersion The version
     */
    public final void setVersion(Version theVersion)
    {
        this.version = theVersion.getValue();
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Adds the libraries required by Cactus on the server side.
     */
    private void addCactusJars()
    {
        addJarWithClass("org.aspectj.lang.JoinPoint", "AspectJ Runtime");
        addJarWithClass("org.apache.cactus.ServletTestCase",
            "Cactus Framework");
        addJarWithClass("org.apache.commons.logging.Log", "Commons-Logging");
        addJarWithClass("org.apache.commons.httpclient.HttpClient",
            "Commons-HttpClient");
        addJarWithClass("junit.framework.TestCase", "JUnit");
    }

    /**
     * Adds the JAR file containing the specified resource to the WEB-INF/lib
     * folder of a web-application archive.
     * 
     * @param theClassName The name of the class that the JAR contains
     * @param theDescription A description of the JAR that should be displayed
     *        to the user in log messages
     */
    private void addJarWithClass(String theClassName, String theDescription)
    {
        // TODO: only add a JAR if the source WAR doesn't already contain an
        // equivalent JAR. We'd probably determine equivalence by looking at the
        // 'Extension-Name' attributes in the JARs MANIFESTs
        String resourceName = "/" + theClassName.replace('.', '/') + ".class";
        ZipFileSet jar = new ZipFileSet();
        File file = ResourceUtils.getResourceLocation(resourceName);
        if (file != null)
        {
            jar.setFile(file);
            addLib(jar);
        }
        else
        {
            log("Could not find the " + theDescription + " JAR",
                Project.MSG_WARN);
            log("You need to add the JAR to the classpath of the task",
                Project.MSG_INFO);
            log("(Searched for class " + theClassName + ")", Project.MSG_DEBUG);
        }
    }

    /**
     * Adds the Cactus JSP redirector file to the web application.
     */
    private void addJspRedirector()
    {
        // Now copy the actual JSP redirector file into the web application
        File jspRedirectorFile = new File(
            new File(System.getProperty("java.io.tmpdir")),
            "jspRedirector.jsp");
        jspRedirectorFile.deleteOnExit();
        try
        {
            ResourceUtils.copyResource(getProject(),
                "/org/apache/cactus/server/jspRedirector.jsp",
                jspRedirectorFile);
        }
        catch (IOException e)
        {
            log("Could not copy the JSP redirector (" + e.getMessage() + ")",
                Project.MSG_WARN);
        }
        FileSet fileSet = new FileSet();
        fileSet.setFile(jspRedirectorFile);
        addFileset(fileSet);
    }

    /**
     * Enhances the provided web deployment descriptor with the definitions 
     * required for testing with Cactus.
     * 
     * @param theWebXml The original deployment descriptor
     * @return A temporary file containing the cactified descriptor
     */
    private File cactifyWebXml(WebXml theWebXml)
    {
        // Add the redirectors the the web application
        WebXml redirectorsWebXml = getPrototypeWebXml(theWebXml);
        WebXmlMerger merger = new WebXmlMerger(theWebXml);
        merger.setLog(new AntLog(this));
        merger.merge(redirectorsWebXml);
        addJspRedirector();
        
        // If the user has specified a deployment descriptor to merge into the
        // cactified descriptor, perform the merge 
        if (this.mergeWebXml != null)
        {
            try
            {
                WebXml parsedMergeWebXml = WebXmlIo.parseWebXmlFromFile(
                    this.mergeWebXml, this.xmlCatalog);
                merger = new WebXmlMerger(theWebXml);
                merger.setLog(new AntLog(this));
                merger.merge(parsedMergeWebXml);
            }
            catch (IOException e)
            {
                throw new BuildException(
                    "Could not merge deployment descriptors", e);
            }
            catch (SAXException e)
            {
                throw new BuildException("Parsing of merge file failed", e);
            }
            catch (ParserConfigurationException e)
            {
                throw new BuildException("XML parser configuration error", e);
            }
        }
        
        // Serialize the cactified deployment descriptor into a temporary file,
        // so that it can get picked up by the War task
        FileUtils fileUtils = FileUtils.newFileUtils();
        File tmpWebXml = fileUtils.createTempFile("cactus", "web.xml",
            getProject().getBaseDir());
        tmpWebXml.deleteOnExit();
        try
        {
            WebXmlIo.writeWebXml(theWebXml, tmpWebXml);
        }
        catch (IOException ioe)
        {
            throw new BuildException(
                "Could not write temporary deployment descriptor", ioe);
        }
        return tmpWebXml;
    }

    /**
     * Extracts and parses the original web deployment descriptor from the
     * web-app.
     * 
     * @return The parsed descriptor
     * @throws BuildException If the descriptor is not found or could not be 
     *         parsed
     */
    private WebXml getOriginalWebXml() throws BuildException
    {
        // Open the archive as JAR file and extract the deployment descriptor
        WarArchive war = null;
        try
        {
            war = new WarArchive(this.srcFile);
            WebXml webXml = war.getWebXml();
            if (webXml == null)
            {
                throw new BuildException(
                    "The source file does not contain a deployment descriptor");
            }
            return webXml;
        }
        catch (SAXException e)
        {
            throw new BuildException(
                "Parsing of deployment descriptor failed", e);
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open WAR", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("XML parser configuration error", e);
        }
    }

    /**
     * Returns a prototype deployment descriptor containing definitions
     * corresponding to the nested redirector elements. 
     * 
     * @param theWebXml The original deployment descriptor
     * @return The descriptor with the redirector definitions
     * @throws BuildException If an error ocurrs with JAXP initialization
     */
    private WebXml getPrototypeWebXml(WebXml theWebXml) throws BuildException
    {
        try
        {
            WebXml webXml = WebXmlIo.newWebXml(theWebXml.getVersion());

            // Add the filter redirector
            webXml.addFilter("FilterRedirector", FILTER_REDIRECTOR_CLASS);
            if (this.filterRedirector != null)
            {
                webXml.addFilterMapping("FilterRedirector",
                    this.filterRedirector.getMapping());
            }
            else
            {
                webXml.addFilterMapping("FilterRedirector",
                    DEFAULT_FILTER_REDIRECTOR_MAPPING);
            }

            // Add the JSP redirector
            webXml.addJspFile("JspRedirector", "/jspRedirector.jsp");
            if (this.jspRedirector != null)
            {
                webXml.addServletMapping("JspRedirector",
                    this.jspRedirector.getMapping());
            }
            else
            {
                webXml.addServletMapping("JspRedirector",
                    DEFAULT_JSP_REDIRECTOR_MAPPING);
            }

            // Add the servlet redirector
            webXml.addServlet("ServletRedirector", SERVLET_REDIRECTOR_CLASS);
            if (this.servletRedirector != null)
            {
                webXml.addServletMapping("ServletRedirector",
                    this.servletRedirector.getMapping());
            }
            else
            {
                webXml.addServletMapping("ServletRedirector",
                    DEFAULT_SERVLET_REDIRECTOR_MAPPING);
            }

            return webXml;
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException(
                "Could not parse deployment descriptor", e);
        }
    }

}
