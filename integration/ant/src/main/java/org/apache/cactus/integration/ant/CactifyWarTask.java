/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.cactus.integration.api.cactify.FilterRedirector;
import org.apache.cactus.integration.api.cactify.JspRedirector;
import org.apache.cactus.integration.api.cactify.Redirector;
import org.apache.cactus.integration.api.cactify.ServletRedirector;
import org.apache.cactus.integration.api.version.Version;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.types.ZipFileSet;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.merge.WebXmlMerger;
import org.codehaus.cargo.util.log.AntLogger;
import org.jdom.JDOMException;

/**
 * An Ant task that injects elements necessary to run Cactus tests into an
 * existing WAR file.
 * 
 * @version $Id: CactifyWarTask.java 394252 2006-04-15 04:20:17Z felipeal $
 */
public class CactifyWarTask extends War
{

    // Constants ---------------------------------------------------------------
    
    /**
     * Context of the cactus web application.
     */
    private String context;
    
    /**
     * Name of the generated web app file.
     */
    private String FILE_NAME = "cactus.war";
    
    /**
     * Get some non-crypto-grade randomness from various places.
     */
    private static Random rand = new Random(System.currentTimeMillis()
            + Runtime.getRuntime().freeMemory());
    
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
     * The Cactus test redirectors.
     */
    private List redirectors = new ArrayList();

    /**
     * For resolving entities such as DTDs.
     */
    private XMLCatalog xmlCatalog = null;

    /**
     * The web-app version to use when creating a WAR from scratch.
     */
    private String version = null;
    
    /**
     * List of ejb-refs to add to the deployment descriptor 
     * of the cactified war.
     */
    private List ejbRefs = new ArrayList();
    /**
     * The cargo ResourceUtils.
     */
    private ResourceUtils utils = new ResourceUtils();

    // Public Methods ----------------------------------------------------------

    
    /**
     * {@inheritDoc}
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        WebXml webXml = null;
        if (this.srcFile != null)
        {
            log("Analyzing war: " + this.srcFile.getAbsolutePath(),
                Project.MSG_INFO);

            // Add everything that's in the source WAR to the destination WAR
            ZipFileSet currentFiles = new ZipFileSet();
            currentFiles.setSrc(this.srcFile);
            currentFiles.createExclude().setName("WEB-INF/web.xml");
            currentFiles.createExclude().setName("WEB-INF/weblogic.xml");
            currentFiles.createExclude().setName("WEB-INF/orion-web.xml");
            currentFiles.createExclude().setName("WEB-INF/ibm-web-bnd.xmi");
            addZipfileset(currentFiles);

            // Parse the original deployment descriptor
            try 
            {
                webXml = getOriginalWebXml();

            }
            catch (JDOMException e) 
            {
                throw new BuildException("Unable to get the original exception", e);
            }
        }
        if (this.srcFile == null || webXml == null)
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
            else if (this.version.equals("2.3"))
            {
                webXmlVersion = WebXmlVersion.V2_3;
            } 
            else 
            {
                webXmlVersion = WebXmlVersion.V2_4;
            }
            
            webXml = WebXmlIo.newWebXml(webXmlVersion);
        }
        
        File tmpWebXml = null;
        try 
        {
            tmpWebXml = cactifyWebXml(webXml);
        } 
        catch (JDOMException e) 
        {
            throw new BuildException("Unable to cactify your application.", e);
        }
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
     * Adds a Cactus filter test redirector.
     * 
     * @param theFilterRedirector The redirector to add
     */
    public final void addFilterRedirector(FilterRedirector theFilterRedirector)
    {
        this.redirectors.add(theFilterRedirector);
    }

    /**
     * Adds a Cactus JSP test redirector.
     * 
     * @param theJspRedirector The redirector to add
     */
    public final void addJspRedirector(JspRedirector theJspRedirector)
    {
        this.redirectors.add(theJspRedirector);
    }

    /**
     * Adds a Cactus servlet test redirector.
     * 
     * @param theServletRedirector The redirector to add
     */
    public final void addServletRedirector(
        ServletRedirector theServletRedirector)
    {
        this.redirectors.add(theServletRedirector);
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
     * Adds a configured EjbRef instance. Called by Ant.
     * 
     * @param theEjbRef the EjbRef to add
     */
    public final void addConfiguredEjbref(EjbRef theEjbRef)
    {
        ejbRefs.add(theEjbRef);
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
        addJarWithClass("org.apache.commons.logging.Log",
            "Commons-Logging");
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
        String resourceName = "/" + theClassName.replace('.', '/') + ".class";
        if (this.srcFile != null)
        {
            try
            {
                WarArchive srcWar = new DefaultWarArchive(
                    new FileInputStream(srcFile));
                if (srcWar.containsClass(theClassName))
                {
                    log("The " + theDescription + " JAR is already present in "
                        + "the WAR", Project.MSG_VERBOSE);
                    return;
                }
            }
            catch (IOException ioe)
            {
                log("Problem reading source WAR to when trying to detect "
                    + "already present JAR files (" + ioe + ")",
                    Project.MSG_WARN);
            }
        }
        ZipFileSet jar = new ZipFileSet();

        File file = utils.getResourceLocation(resourceName);
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
            utils.copyResource("/org/apache/cactus/server/jspRedirector.jsp",
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
     * Adds the definitions corresponding to the nested redirector elements to
     * the provided deployment descriptor. 
     * 
     * @param theWebXml The deployment descriptor
     */
    private void addRedirectorDefinitions(WebXml theWebXml)
    {
        boolean filterRedirectorDefined = false;
        boolean jspRedirectorDefined = false;
        boolean servletRedirectorDefined = false;
        
        // add the user defined redirectors
        for (Iterator i = this.redirectors.iterator(); i.hasNext();)
        {
        
            Redirector redirector = (Redirector) i.next();
            if (redirector instanceof FilterRedirector)
            {
                filterRedirectorDefined = true;
            }
            else if (redirector instanceof JspRedirector)
            {
                jspRedirectorDefined = true;
            }
            else if (redirector instanceof ServletRedirector)
            {
                servletRedirectorDefined = true;
            }
            redirector.mergeInto(theWebXml);
        }

        // now add the default redirectors if they haven't been provided by
        // the user
        if (!filterRedirectorDefined)
        {
            new FilterRedirector(new AntLogger(getProject()))
                .mergeInto(theWebXml);
        }
        if (!servletRedirectorDefined)
        {
            new ServletRedirector(new AntLogger(getProject()))
                .mergeInto(theWebXml);
        }
        if (!jspRedirectorDefined)
        {
            new JspRedirector(new AntLogger(getProject())).mergeInto(theWebXml);
        }
    }

    /**
     * Enhances the provided web deployment descriptor with the definitions 
     * required for testing with Cactus.
     * 
     * @param theWebXml The original deployment descriptor
     * @return A temporary file containing the cactified descriptor
     * @throws JDOMException in case a JDOM exception is thrown.
     */
    private File cactifyWebXml(WebXml theWebXml) throws JDOMException
    {
        addRedirectorDefinitions(theWebXml);
        addJspRedirector();
        addEjbRefs(theWebXml);
        
        // If the user has specified a deployment descriptor to merge into the
        // cactified descriptor, perform the merge 
        if (this.mergeWebXml != null)
        {
            try
            {
                WebXml parsedMergeWebXml = WebXmlIo.parseWebXmlFromFile(
                    this.mergeWebXml, this.xmlCatalog);
                WebXmlMerger merger = new WebXmlMerger(theWebXml);
                merger.setLogger(new AntLogger(this));
               
                merger.merge(parsedMergeWebXml);
            }
            catch (IOException e)
            {
                throw new BuildException(
                    "Could not merge deployment descriptors", e);
            }
        }
        
        // Serialize the cactified deployment descriptor into a temporary file,
        // so that it can get picked up by the War task
        //FileUtils fileUtils = FileUtils.newFileUtils();
        File tmpDir = createTempFile("cactus", "tmp.dir",
            getProject().getBaseDir(), true);
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
        File webXmlFile = null;
        try
        {
            ZipFileSet fileSet = new ZipFileSet();
            fileSet.setDir(tmpDir);
            tmpDir.mkdir();
            File[] files = WebXmlIo.writeAll(theWebXml, 
                tmpDir.getAbsolutePath());
        
            
            for (int i = 0; i < files.length; i++)
            {
                File f = files[i];
                f.deleteOnExit();
                if (f.getName().equals("web.xml"))
                {
                    webXmlFile = f;
                }
                else
                {
                    fileSet.createInclude().setName(f.getName());
                }
            }
            addWebinf(fileSet);
        }
        catch (IOException ioe)
        {
            throw new BuildException(
                "Could not write temporary deployment descriptor", ioe);
        }
        return webXmlFile;
    }

    /**
     * Extracts and parses the original web deployment descriptor from the
     * web-app.
     * 
     * @return The parsed descriptor or null if not found
     * @throws BuildException If the descriptor could not be 
     *         parsed
     * @throws JDOMException in case is JDOM exception is thrown.
     */
    private WebXml getOriginalWebXml() throws BuildException, JDOMException
    {
        // Open the archive as JAR file and extract the deployment descriptor
        WarArchive war = null;
        try
        {
            war = new DefaultWarArchive(new FileInputStream(this.srcFile));
            WebXml webXml = war.getWebXml();
            return webXml;
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open WAR", e);
        }
    }

    /**
     * Add ejb references to a web.xml.
     * 
     * @param theWebXml the web.xml to modify
     */
    private void addEjbRefs(WebXml theWebXml)
    {
        Iterator i = ejbRefs.iterator();
        while (i.hasNext())
        {
            EjbRef ref = (EjbRef) i.next();
            WebXmlUtils.addEjbRef(theWebXml, ref);
        }
    }
    
    /**
     * A method to create the temporary files.
     * @param thePrefix the prefix of the filename.
     * @param theSuffix the suffix of the filename
     * @param theParentDir the parent directory
     * @param isDeleteOnExit should we delete the directories on exit?
     * @return the temporary file
     */
    public File createTempFile(String thePrefix, String theSuffix, 
                                   File theParentDir, boolean isDeleteOnExit) 
    {
    File result = null;
    String parent = (theParentDir == null)
            ? System.getProperty("java.io.tmpdir")
            : theParentDir.getPath();

        DecimalFormat fmt = new DecimalFormat("#####");
        synchronized (rand) 
        {
            do 
            {
                result = new File(parent,
                   thePrefix + fmt.format(Math.abs(rand.nextInt()))
                   + theSuffix);
            } 
            while (result.exists());
        }
        if (isDeleteOnExit) 
        {
            result.deleteOnExit();
        }
        return result;
    }
    
    /**
     * Gets the file name.
     * 
     * @return the name of the web app file
     */
    public String getFileName()
    {
        return FILE_NAME;
    }

    /**
     * Returns the context.
     * 
     * @return <code>java.lang.String</code>
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the context.
     * 
     * @param context
     */
    public void setContext(String context) {
        this.context = context;
    }
    
    
}
