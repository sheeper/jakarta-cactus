/* 
 * ========================================================================
 * 
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.deployment.application.ApplicationXml;
import org.apache.cactus.integration.ant.deployment.application.ApplicationXmlIo;
import org.apache.cactus.integration.ant.deployment.application.DefaultEarArchive;
import org.apache.cactus.integration.ant.deployment.application.EarArchive;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ear;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.xml.sax.SAXException;

/**
 * An Ant task that injects elements necessary to run Cactus tests into an
 * existing EAR file.
 * 
 * @version $Id$
 */
public class CactifyEarTask extends Ear
{
    /**
     * Cactus war configuration holder
     */
    private CactusWar cactusWar;
    
    /**
     * The archive that contains the web-app that should be cactified.
     */
    private File srcFile;
    
    /**
     * 
     * @param theCactusWar CactusWar to set
     */
    public void addConfiguredCactuswar(CactusWar theCactusWar)
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
        ApplicationXml appXml = getOriginalApplicationXml();
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
     */
    private ApplicationXml getOriginalApplicationXml()
    {
        ApplicationXml appXml = null;
        try
        {
            EarArchive ear = new DefaultEarArchive(this.srcFile);
            appXml = ear.getApplicationXml();
            if (appXml == null)
            {
                throw new BuildException("The EAR source file does not "
                    + "contain a META-INF/application.xml " 
                    + "deployment descriptor");
            }
        }
        catch (SAXException e)
        {
            throw new BuildException(
                "Parsing of application.xml deployment descriptor failed", e);
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open EAR", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("XML parser configuration error", e);
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
    private CactusWar createCactusWarConfig()
    {
        CactusWar cactusWarConfig = new CactusWar();
        CactusWar.Version version = new CactusWar.Version();
        version.setValue("2.3");
        cactusWarConfig.setVersion(version);
        cactusWarConfig.setContext("/cactus");
        cactusWarConfig.setProject(getProject());
        
        return cactusWarConfig;
    }
}
