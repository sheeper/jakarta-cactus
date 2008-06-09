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
import java.util.Iterator;

import org.apache.cactus.integration.ant.CactusWar;
import org.apache.cactus.integration.api.version.Version;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.ApplicationXmlIo;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.application.EarArchive;
import org.codehaus.cargo.module.ejb.EjbArchive;
import org.codehaus.cargo.module.ejb.EjbJarXml;
import org.codehaus.cargo.module.ejb.Entity;
import org.codehaus.cargo.module.ejb.Session;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ear.EarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.util.FileUtils;

import org.jdom.JDOMException;
/**
 * A maven2 mojo that injects elements necessary to run Cactus tests into an
 * existing EAR file.
 * 
 * @version $Id: CactifyEarMojo.java 394252 2008-04-29 04:20:17Z ptahchiev $
 * @goal cactifyear
 * @requiresDependencyResolution compile
 */
public class CactifyEarMojo extends AbstractMojo
{
    /**
     * Cactus war configuration holder.
     */
    private CactusWar cactusWar;
    
    /**
     * The archive that contains the web-app that should be cactified.
     */
    private File srcFile;

    /**
     * Indicates whether or not we should add ejb references to local ejbs
     * in the deployment descriptor.
     */
    private boolean addEjbReferences;
    
    /**
     * The Ear archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#ear}"
     * @required
     */
    private EarArchiver earArchiver;
    
    /**
     * The maven archive configuration to use.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The "main" method of the mojo.
     * @throws MojoExecutionException in case an error occurs.
     * @throws MojoFailureException in case a failure occurs.
     */
	public void execute() throws MojoExecutionException, MojoFailureException 
	{
        if (cactusWar == null)
        {
            cactusWar = createCactusWarConfig();
        }
        
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(earArchiver);
        
        //Add everything that's in the source EAR to the destination EAR
        File tempLocation = null;
        
        tempLocation = FileUtils.createTempFile("cactus", "explode.tmp.dir",
                getProject().getBasedir());
        tempLocation.mkdirs();
        tempLocation.deleteOnExit();
        
        //ZipFileSet currentFiles = new ZipFileSet();
        //currentFiles.setSrc(this.srcFile);
        //currentFiles.createExclude().setName("META-INF/application.xml");
        //addZipfileset(currentFiles);
        
        // cactify the application.xml
        ApplicationXml appXml = null;
        try 
        {
            appXml = getOriginalApplicationXml();
        } 
        catch (JDOMException e) 
        {
            throw new MojoExecutionException("Unable to get the "
               + "original application.xml", e);
        }
        
        File tmpAppXml = cactifyApplicationXml(appXml);
        
        try 
        {
            earArchiver.addDirectory(tempLocation);
            earArchiver.setAppxml(tmpAppXml);
            archiver.createArchive(getProject(), getArchive());
    
            
            // create the cactus war
            File cactusWarFile = createCactusWar();
            addFileToEar(cactusWarFile, cactusWar.getFileName());
        }
        catch(ArchiverException aex)
        {
            throw new MojoExecutionException("Error while performing the "
                    + "cactified archive.", aex);
        } 
        catch (ManifestException me) 
        {
            throw new MojoExecutionException("Error reading the manifest "
                   + "data in the original file.", me);
        } 
        catch (IOException ioe) 
        {
            throw new MojoExecutionException("Input/output exception occured ",
                    ioe);
        }
        catch (DependencyResolutionRequiredException de) 
        {
            throw new MojoExecutionException("Problem with resolving the"
                    + " dependencies of the project. ", de);
        }
        
        //super.execute();
	}
    
    /**
     * A helper method to create a temporary file.
     * @return the cactus.war
     * @throws MojoExecutionException in case a runtime error occurs.
     */
    private File createCactusWar() throws MojoExecutionException
    {
        File tmpCactusWar = FileUtils.createTempFile("cactus", "cactus.war",
                                                     getProject().getBasedir());
        tmpCactusWar.deleteOnExit();
        cactusWar.setDestFile(tmpCactusWar);
        
        if (addEjbReferences)
        {
            try 
            {
                addEjbReferencesToWar(tmpCactusWar);
            } 
            catch (JDOMException e) 
            {
                throw new MojoExecutionException(
                        "Unable to add ejb-references", e);
            }
        }
        
        cactusWar.execute();
        
        return tmpCactusWar;
    }
    
    /**
     * 
     * @param theAppXml the application.xml to cactify
     * @return the cactified application.xml
     * @throws MojoExecutionException 
     */
    private File cactifyApplicationXml(ApplicationXml theAppXml) 
                                            throws MojoExecutionException
    {
        theAppXml.addWebModule(cactusWar.getFileName(), cactusWar.getContext());
        // serialize the cactified app xml
        //FileUtils fileUtils = FileUtils.newFileUtils();
        File tmpAppXml = FileUtils.createTempFile("cactus", "application.xml",
                                                  getProject().getBasedir());
        tmpAppXml.deleteOnExit();
        try
        {
            ApplicationXmlIo.writeApplicationXml(theAppXml, 
                                                 tmpAppXml, 
                                                 null, true);
        }
        catch (IOException ioe)
        {
            throw new MojoExecutionException(
                "Could not write temporary deployment descriptor", ioe);
        }
        return tmpAppXml;
    }
    
    /**
     * 
     * @return the application.xml from the source ear
     * @throws JDOMException in case a JDOM exception is thrown
     * @throws MojoExecutionException 
     */
    private ApplicationXml getOriginalApplicationXml() 
                            throws JDOMException, MojoExecutionException
    {
        ApplicationXml appXml = null;
        try
        {
            EarArchive ear = new DefaultEarArchive(
                new FileInputStream(this.srcFile));
            appXml = ear.getApplicationXml();
            if (appXml == null)
            {
                throw new MojoExecutionException("The EAR source file does not "
                    + "contain a META-INF/application.xml " 
                    + "deployment descriptor");
            }
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Failed to open EAR", e);
        }
        
        return appXml;
    }

    /**
     * 
     * @param theFile the file to add
     * @param theFullPath the path within the ear
     * @throws ArchiverException in case the adding is impossible.
     */
    private void addFileToEar(File theFile, String theFullPath) 
                                                    throws ArchiverException
    {
        earArchiver.addFile(theFile, theFullPath);
    }
    
    /**
     * Add ejb references.
     * 
     * @param theWar temporary cactus war
     * @throws JDOMException in case a parse exception is thrown 
     * @throws MojoExecutionException in case any other error occurs.
     */
    private void addEjbReferencesToWar(File theWar) 
                    throws JDOMException, MojoExecutionException 
    {
        try
        {
            EarArchive ear = new DefaultEarArchive(
                new FileInputStream(srcFile));
            ApplicationXml appXml = ear.getApplicationXml();
            Iterator ejbModules = appXml.getEjbModules();
            while (ejbModules.hasNext())
            {
                String module = (String) ejbModules.next();
                EjbArchive ejbArchive = ear.getEjbModule(module);
                EjbJarXml descr = ejbArchive.getEjbJarXml();
                Iterator vendorDescrIterator = descr.getVendorDescriptors();
                if (vendorDescrIterator == null 
                    || !vendorDescrIterator.hasNext())
                {
                    throw new MojoExecutionException("Failed to find vendor " 
                                             + "deployment descriptor " 
                                             + "for ejb jar " + module);
                }
                
                Iterator ejbs = descr.getSessionEjbs();
                while (ejbs.hasNext())
                {
                    Session ejb = (Session) ejbs.next();
                    String name = ejb.getName();
                    String local = ejb.getLocal();
                    String localHome = ejb.getLocalHome();
                    if (local != null)
                    {
                        getLog().debug("Adding ejb-ref for local session ejb "
                            + ejb.getName());
                        EjbRef ref = new EjbRef();
                        ref.setType("Session");
                        ref.setEjbName(name);
                        ref.setName("ejb/" + name);
                        ref.setEjbInterface(local);
                        ref.setEjbHomeInterface(localHome);
                        ref.setLocal(true);
                        
                        cactusWar.addConfiguredEjbref(ref);
                    }
                }
                ejbs = descr.getEntityEjbs();
                while (ejbs.hasNext())
                {
                    Entity ejb = (Entity) ejbs.next();
                    String name = ejb.getName();
                    String local = ejb.getLocal();
                    String localHome = ejb.getLocalHome();
                    if (local != null)
                    {
                        getLog().debug("Adding ejb-ref for local entity ejb "
                            + ejb.getName());
                        EjbRef ref = new EjbRef();
                        ref.setType("Entity");
                        ref.setEjbName(name);
                        ref.setName("ejb/" + name);
                        ref.setEjbInterface(local);
                        ref.setEjbHomeInterface(localHome);
                        ref.setLocal(true);
                        
                        cactusWar.addConfiguredEjbref(ref);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Could not merge deployment " 
                                     + "descriptors", e);
        }
    }
    
    /**
     * Initialize cactusWar with some default values.
     *
     * @return the CactusWar configuration
     */
    private CactusWar createCactusWarConfig()
    {
        CactusWar cactusWarConfig = new CactusWar();
        Version version = new Version();
        version.setValue("2.3");
        cactusWarConfig.setVersion(version);
        cactusWarConfig.setContext("/cactus");
        cactusWarConfig.setProject(getProject());
        
        return cactusWarConfig;
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
