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
package org.apache.cactus.maven2.mojos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.cactus.integration.api.cactify.CactifyUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.archive.ArchiveExpansionException;
import org.apache.maven.plugin.assembly.utils.AssemblyFileUtils;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.types.XMLCatalog;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.merge.WebXmlMerger;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.jdom.JDOMException;

/**
 * A maven2 mojo that injects elements necessary to run Cactus tests into an
 * existing WAR file.
 * 
 * @version $Id: CactifyWarMojo.java 394252 2008-04-29 04:20:17Z ptahchiev $
 * @goal cactifywar
 * @requiresDependencyResolution compile
 */
public class CactifyWarMojo extends AbstractMojo
{
	
    /**
     * Get some non-crypto-grade randomness from various places.
     */
    private static Random rand = new Random(System.currentTimeMillis()
            + Runtime.getRuntime().freeMemory());

    /**
     * For resolving entities such as DTDs.
     */
    private XMLCatalog xmlCatalog = null;

    /**
     * The archive that contains the web-app that should be cactified.
     * @parameter
     */
    private File srcFile;
    
    /**
     * The War archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
     * @required
     */
    private WarArchiver warArchiver;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The maven archive configuration to use.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Location of the descriptor of which the content should be merged into 
     * the descriptor of the cactified archive.
     * @parameter
     */
    private File mergeWebXml;

    /**
     * The Cactus test redirectors.
     * @parameter
     */
    private List redirectors = new ArrayList();
    
    /**
     * List of ejb-refs to add to the deployment descriptor 
     * of the cactified war.
     */
    private List ejbRefs = new ArrayList();
    
    /**
     * The cargo ResourceUtils.
     */
    private ResourceUtils utils = new ResourceUtils();
    
    /**
     * The archive manager.
     * @component
     */
    private ArchiverManager archiverManager;
    
    /**
     * Dependencies to be included in the WEB-INF/lib folder.
     * @parameter
     */
    private List libDependencies;
    
    /**
     * Should we install the cactified archive in the local maven repo?
     * @parameter
     */
    private boolean installLocally = false;
    
    /**
     * @plexus.requirement
     */
    private ArtifactFactory factory;
  
    /**
     * The file that we want to produce.
     * @parameter
     */
    private File destFile;
    
    /**
     * The "main" method of the mojo.
     * @throws MojoExecutionException in case an error occurs.
     * @throws MojoFailureException in case a failure occurs.
     */
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		
		if(this.srcFile != null) 
		{
            getLog().info("Analyzing war: " + this.srcFile.getAbsolutePath());
		}
		
        WebXml webXml = null;
        
        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver( warArchiver );

        archiver.setOutputFile( destFile );
        
        File tmpWebXml = null;
        
		try 
		{
			webXml = getOriginalWebXml();
			tmpWebXml = cactifyWebXml(webXml);
			
			//Add the required libs for Cactus.
			warArchiver.addLib(addJarWithClass("org.aspectj.lang.JoinPoint", 
					"AspectJ Runtime"));
			warArchiver.addLib(addJarWithClass("org.apache.cactus." +
					"ServletTestCase", "Cactus Framework"));
			warArchiver.addLib(addJarWithClass("org.apache.commons.logging.Log",
		            "Commons-Logging"));
			warArchiver.addLib(addJarWithClass("org.apache.commons." +
					"httpclient.HttpClient", "Commons-HttpClient"));
			warArchiver.addLib(addJarWithClass("junit.framework." +
					"TestCase", "JUnit"));
			
	        File tempLocation = createTempFile("cactus", "explode.tmp.dir",
	                getProject().getBasedir(), true);
			
			tempLocation.mkdirs();
			tempLocation.deleteOnExit();
			
			//Now add all of the additional lib files.
			for (Iterator iter = libDependencies.iterator();iter.hasNext();)
			{
				org.apache.cactus.maven2.mojos.Dependency dependency = 
					(org.apache.cactus.maven2.mojos.Dependency) iter.next();
				warArchiver.addLib(new File(dependency.getDependencyPath(
					project, getLog())));
			}
			
			try {
				AssemblyFileUtils.unpack( this.srcFile, tempLocation,
						archiverManager );
			} catch (ArchiveExpansionException e) {
	        	throw new MojoExecutionException("Error extracting the" +
	        			" archive.", e);
			} catch (NoSuchArchiverException e) {
	        	throw new MojoExecutionException("Problem reading the " +
	        			"source archive.", e);
			}
			warArchiver.addDirectory(tempLocation);
			warArchiver.setWebxml(tmpWebXml);
			archiver.createArchive( getProject(), getArchive() );
			
			FileUtils.deleteDirectory(tempLocation);
		} 
		catch (ArchiverException e) 
		{
        	throw new MojoExecutionException("Problem reading the " +
        			"source archive.", e);
		} 
        catch (JDOMException e) 
		{
        	throw new MojoExecutionException("Unable to cactify " +
        			"your web.xml.", e);
		}
		catch (ManifestException e) 
		{
        	throw new MojoExecutionException("Problem reading the " +
        			"source archive.", e);
		} 
		catch (IOException e) 
		{
        	throw new MojoExecutionException("Input/output error reading the" +
        			"source archive.", e);
		} 
		catch (DependencyResolutionRequiredException e) 
		{
        	throw new MojoExecutionException("Error resolving your " +
        			"dependencies", e);
		}
	}
	
    /**
     * Enhances the provided web deployment descriptor with the definitions 
     * required for testing with Cactus.
     * 
     * @param theWebXml The original deployment descriptor
     * @return A temporary file containing the cactified descriptor
     * @throws JDOMException in case a JDOM exception is thrown.
     * @throws MojoExecutionException in case any other error occurs.
     */
    private File cactifyWebXml(WebXml theWebXml) throws JDOMException, 
    													MojoExecutionException
    {
    	CactifyUtils utils = new CactifyUtils();
    	utils.addRedirectorDefinitions(theWebXml, redirectors);
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
                merger.setLogger(utils.getLogger());
               
                merger.merge(parsedMergeWebXml);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(
                    "Could not merge deployment descriptors", e);
            }
        }
        
        // Serialize the cactified deployment descriptor into a temporary file,
        // so that it can get picked up by the War task
        //FileUtils fileUtils = FileUtils.newFileUtils();
        File tmpDir = createTempFile("cactus", "tmp.dir",
            getProject().getBasedir(), true);
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
        File webXmlFile = null;
        try
        {
            tmpDir.mkdir();
            File[] files = WebXmlIo.writeAll(theWebXml, 
                tmpDir.getAbsolutePath());
            List includes = new ArrayList();
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
                    includes.add(f.getName());
                }
            }
            String[] strIncludes = new String[includes.size()];
            int i=0;
            for(Iterator iter = includes.iterator(); iter.hasNext();)
            {
            	strIncludes[i] = iter.next().toString();
            	i++;
            }
            try {
				warArchiver.addWebinf(tmpDir, strIncludes,null);
			} catch (ArchiverException e) {
				throw new MojoExecutionException(
		                "Error reading the source archive.", e);
			}
        }
        catch (IOException ioe)
        {
            throw new MojoExecutionException(
                "Could not write temporary deployment descriptor", ioe);
        }
        return webXmlFile;
    }
    
    /**
     * A method to create the temporary files.
     * 
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
            getLog().warn("Could not copy the JSP redirector (" + 
            		e.getMessage() + ")");
        }
        try {
			warArchiver.addFile(jspRedirectorFile, jspRedirectorFile.getName());
		} catch (ArchiverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Adds the JAR file containing the specified resource to the WEB-INF/lib
     * folder of a web-application archive.
     * 
     * @param theClassName The name of the class that the JAR contains
     * @param theDescription A description of the JAR that should be displayed
     *        to the user in log messages
     */
    private File addJarWithClass(String theClassName, String theDescription)
    {
        String resourceName = "/" + theClassName.replace('.', '/') + ".class";
        if (srcFile != null)
        {
            try
            {
                WarArchive srcWar = new DefaultWarArchive(
                    new FileInputStream(srcFile));
                if (srcWar.containsClass(theClassName))
                {
                    getLog().debug("The " + theDescription + " JAR is " +
                    		"already present in the WAR");
                    return null;
                }
            }
            catch (IOException ioe)
            {
                getLog().warn("Problem reading source WAR to when " +
                	"trying to detect already present JAR files (" + ioe + ")");
            }
        }
        File file = utils.getResourceLocation(resourceName);
        return file;   
    }
    
    /**
     * Extracts and parses the original web deployment descriptor from the
     * web-app.
     * 
     * @return The parsed descriptor or null if not found
     * @throws MojoExecutionException If the descriptor could not be 
     *         parsed
     * @throws JDOMException in case is JDOM exception is thrown.
     */
    private WebXml getOriginalWebXml() throws MojoExecutionException, 
    										  JDOMException
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
            throw new MojoExecutionException("Failed to open WAR", e);
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
     * Getter method for the MavenProject.
     * @return the MavenProject
     */
    public MavenProject getProject()
    {
        return project;
    }
    
    /**
     * Getter method for the MavenArchiveConfiguration.
     * @return the MavenArchiveConfiguration
     */
    public MavenArchiveConfiguration getArchive()
    {
        return archive;
    }
}