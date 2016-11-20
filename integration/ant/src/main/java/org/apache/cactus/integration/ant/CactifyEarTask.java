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

import org.apache.cactus.integration.api.version.Version;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ear;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.ApplicationXmlIo;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.application.EarArchive;
import org.codehaus.cargo.module.ejb.EjbArchive;
import org.codehaus.cargo.module.ejb.EjbJarXml;
import org.codehaus.cargo.module.ejb.Entity;
import org.codehaus.cargo.module.ejb.Session;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * An Ant task that injects elements necessary to run Cactus tests into an
 * existing EAR file.
 * 
 * @version $Id: CactifyEarTask.java 394252 2006-04-15 04:20:17Z felipeal $
 */
public class CactifyEarTask extends Ear
{
    /**
     * Cactus war configuration holder.
     */
    private CactifyWarTask cactusWar;
    
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
     * 
     * @param theCactusWar CactusWar to set
     */
    public void addConfiguredCactuswar(CactifyWarTask theCactusWar)
    {
        cactusWar = theCactusWar;
    }
    
    /**
     * @param theSrcFile The srcFile to set.
     */
    public void setSrcFile(File theSrcFile)
    {
        srcFile = theSrcFile;
    }
    
    /**
     * @return Returns the addEjbReferences.
     */
    public boolean getAddEjbReferences()
    {
        return addEjbReferences;
    }
    /**
     * Indicates whether or not ejb references should be added.
     * If set to true all local ejbs will be accessible via
     * java:comp/env/ejb/<EJB_NAME/>
     * 
     * @param isAddEjbReferences if ejb references should be added.
     */
    public void setAddEjbReferences(boolean isAddEjbReferences)
    {
        this.addEjbReferences = isAddEjbReferences;
    }
    
    /**
     * {@inheritDoc}
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        if (cactusWar == null)
        {
            cactusWar = createCactusWarConfig();
        }
        //Add everything that's in the source EAR to the destination EAR
        ZipFileSet currentFiles = new ZipFileSet();
        currentFiles.setSrc(this.srcFile);
        currentFiles.createExclude().setName("META-INF/application.xml");
        addZipfileset(currentFiles);
        
        // cactify the application.xml
        ApplicationXml appXml = null;
        try 
        {
            appXml = getOriginalApplicationXml();
        } 
        catch (JDOMException e) 
        {
            throw new BuildException("Unable to get the "
               + "original application.xml", e);
        }
        File tmpAppXml = cactifyApplicationXml(appXml);
        setAppxml(tmpAppXml);
        
        // create the cactus war
        File cactusWarFile = createCactusWar();
        addFileToEar(cactusWarFile, cactusWar.getFileName());
        
        super.execute();
    }
    
    /**
     * 
     * @return the application.xml from the source ear
     * @throws JDOMException in case a JDOM exception is thrown
     */
    private ApplicationXml getOriginalApplicationXml() throws JDOMException
    {
        ApplicationXml appXml = null;
        try
        {
            EarArchive ear = new DefaultEarArchive(
                new FileInputStream(this.srcFile));
            appXml = ear.getApplicationXml();
            if (appXml == null)
            {
                throw new BuildException("The EAR source file does not "
                    + "contain a META-INF/application.xml " 
                    + "deployment descriptor");
            }
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open EAR", e);
        }
        
        return appXml;
    }
    
    /**
     * 
     * @param theAppXml the application.xml to cactify
     * @return the cactified application.xml
     */
    private File cactifyApplicationXml(ApplicationXml theAppXml)
    {
        theAppXml.addWebModule(cactusWar.getFileName(), cactusWar.getContext());
        // serialize the cactified app xml
        FileUtils fileUtils = FileUtils.newFileUtils();
        File tmpAppXml = fileUtils.createTempFile("cactus", "application.xml",
                                                  getProject().getBaseDir());
        tmpAppXml.deleteOnExit();
        try
        {
            ApplicationXmlIo.writeApplicationXml(theAppXml, 
                                                 tmpAppXml, 
                                                 null, true);
        }
        catch (IOException ioe)
        {
            throw new BuildException(
                "Could not write temporary deployment descriptor", ioe);
        }
        return tmpAppXml;
    }
    
    /**
     * 
     * @return the cactus.war
     */
    private File createCactusWar()
    {
        FileUtils fileUtils = FileUtils.newFileUtils();
        File tmpCactusWar = fileUtils.createTempFile("cactus", "cactus.war",
                                                     getProject().getBaseDir());
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
                throw new BuildException("Unable to add ejb-references", e);
            }
        }

        cactusWar.execute();
        
        return tmpCactusWar;
    }
    
    /**
     * 
     * @param theFile the file to add
     * @param theFullPath the path within the ear
     */
    private void addFileToEar(File theFile, String theFullPath)
    {
        ZipFileSet fs = new ZipFileSet();
        fs.setFile(theFile);
        fs.setFullpath(theFullPath);
        addZipfileset(fs);
    }
    
    /**
     * Initialize cactusWar with some default values.
     *
     * @return the CactusWar configuration
     */
    private CactifyWarTask createCactusWarConfig()
    {
        CactifyWarTask cactusWarConfig = new CactifyWarTask();
        Version version = new Version();
        version.setValue("2.3");
        cactusWarConfig.setVersion(version);
        cactusWarConfig.setContext("/cactus");
        cactusWarConfig.setProject(getProject());
        
        return cactusWarConfig;
    }

    /**
     * Add ejb references.
     * 
     * @param theWar temporary cactus war
     * @throws JDOMException in case a parse exception is thrown 
     */
    private void addEjbReferencesToWar(File theWar) throws JDOMException 
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
                    throw new BuildException("Failed to find vendor " 
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
                        log("Adding ejb-ref for local session ejb "
                            + ejb.getName(),
                            Project.MSG_VERBOSE);
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
                        log("Adding ejb-ref for local entity ejb "
                            + ejb.getName(),
                            Project.MSG_VERBOSE);
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
            throw new BuildException("Could not merge deployment " 
                                     + "descriptors", e);
        }
    }
}
