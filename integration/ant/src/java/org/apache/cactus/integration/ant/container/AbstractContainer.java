/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container;

import java.io.File;
import java.io.IOException;

import org.apache.cactus.integration.ant.deployment.DeployableFile;
import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Environment.Variable;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Abstract base class for supporting specific containers as nested elements in
 * the {@link org.apache.cactus.integration.ant.CactusTask}.
 * 
 * @version $Id$
 */
public abstract class AbstractContainer extends ProjectComponent
    implements Container
{
    // Constants ---------------------------------------------------------------

    /**
     * The path under which the container resources are stored in the JAR.
     */
    protected static final String RESOURCE_PATH =
        "/org/apache/cactus/integration/ant/container/resources/";

    // Instance Variables ------------------------------------------------------

    /**
     * The WAR or EAR that should be deployed to the container.
     */
    private DeployableFile deployableFile;

    /**
     * A pattern set which lists patterns for names of test cases that are to be
     * excluded from a specific container.
     */
    private PatternSet patternSet = new PatternSet();

    /**
     * The directory to which the test reports should be written.
     */
    private File toDir;

    /**
     * Name of a property that must exist in the project if tests are to be run
     * on the container. 
     */
    private String ifCondition;

    /**
     * Name of a property that must not exist in the project if tests are to be
     * run on the container. 
     */
    private String unlessCondition;

    /**
     * The factory for creating ant tasks.
     */
    private AntTaskFactory antTaskFactory;

    /**
     * The log to use.
     */
    private Log log = AntLog.NULL;

    /**
     * List of system properties to set in the container JVM. 
     */
    private Variable[] systemProperties;

    /**
     * The time to sleep after the container has started up. 
     */
    private long startUpWait = 1000;

    /**
     * Additional classpath entries for the classpath that will be used to 
     * start the containers.
     */
    private Path containerClasspath;    
    
    // Public Methods ----------------------------------------------------------

    /**
     * @see Container#getTestContext()
     */
    public String getTestContext()
    {
        return null;
    }
    
    /**
     * Sets the time to wait after the container has been started up.
     * 
     * The default time is 1 second.
     * 
     * Note: This is a hack while waiting for container specific solutions
     * that tell exactly when the server is started or not. ATM, the only known
     * issue is with JBoss, where the servlet engine is started before the full
     * JBoss is started and thus it may happen that we try to shutdown JBoss 
     * before it has finished starting, leading to an exception.
     * 
     * @param theStartUpWait The time to wait in milliseconds
     */
    public void setStartUpWait(long theStartUpWait)
    {
        this.startUpWait = theStartUpWait;
    }

    /**
     * Creates a nested exclude element that is added to the pattern set.
     * 
     * @return The created exclude element
     */
    public final PatternSet.NameEntry createExclude()
    {
        return this.patternSet.createExclude();
    }

    /**
     * Returns the exclude patterns.
     * 
     * @return The exclude patterns 
     */
    public final String[] getExcludePatterns()
    {
        return this.patternSet.getExcludePatterns(getProject());
    }

    /**
     * Sets the name of a property that must exist in the project if tests are 
     * to be run on the container.
     * 
     * @param theIfCondition The property name to set
     */
    public final void setIf(String theIfCondition)
    {
        this.ifCondition = theIfCondition;
    }

    /**
     * Sets the directory to which the test reports should be written.
     * 
     * @param theToDir The output directory to set
     */
    public final void setToDir(File theToDir)
    {
        this.toDir = theToDir;
    }

    /**
     * Sets the name of a property that must not exist in the project if tests
     * are to be run on the container.
     * 
     * @param theUnlessCondition The property name to set
     */
    public final void setUnless(String theUnlessCondition)
    {
        this.unlessCondition = theUnlessCondition;
    }

    // Container Implementation ------------------------------------------------

    /**
     * @see Container#getStartUpWait 
     */
    public long getStartUpWait()
    {
        return this.startUpWait;
    }

    /**
     * @see Container#getToDir
     */
    public final File getToDir()
    {
        return this.toDir;
    }

    /**
     * The default implementation does nothing.
     * 
     * @see Container#init
     */
    public void init()
    {
        // The default implementation doesn nothing
    }

    /**
     * @see Container#isEnabled
     */
    public final boolean isEnabled()
    {
        return (testIfCondition() && testUnlessCondition());
    }

    /**
     * @see Container#isExcluded
     */
    public final boolean isExcluded(String theTestName)
    {
        String[] excludePatterns =
            this.patternSet.getExcludePatterns(getProject());
        if (excludePatterns != null)
        {
            String testPath = theTestName.replace('.', '/');
            for (int i = 0; i < excludePatterns.length; i++)
            {
                String excludePattern = excludePatterns[i];
                if (excludePattern.endsWith(".java")
                 || excludePattern.endsWith(".class"))
                {
                    excludePattern = excludePattern.substring(
                        0, excludePattern.lastIndexOf('.'));
                }
                if (SelectorUtils.matchPath(excludePattern, testPath))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @see Container#setAntTaskFactory
     */
    public final void setAntTaskFactory(AntTaskFactory theFactory)
    {
        this.antTaskFactory = theFactory;
    }

    /**
     * @see Container#setDeployableFile
     */
    public final void setDeployableFile(DeployableFile theDeployableFile)
    {
        this.deployableFile = theDeployableFile;
    }

    /**
     * @see Container#setLog
     */
    public final void setLog(Log theLog)
    {
        this.log = theLog;
    }

    /**
     * @see Container#setSystemProperties
     */
    public void setSystemProperties(Variable[] theProperties)
    {
        this.systemProperties = theProperties;
    }

    /**
     * @see Container#getSystemProperties
     */
    public Variable[] getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * @see Container#setContainerClasspath(Path)
     * @since Cactus 1.6
     */
    public void setContainerClasspath(Path theClasspath)
    {
        this.containerClasspath = theClasspath;
    }    

    /**
     * @see Container#getContainerClasspath()
     * @since Cactus 1.6
     */
    public Path getContainerClasspath()
    {
        return this.containerClasspath;
    }
    
    // Protected Methods -------------------------------------------------------

    /**
     * Creates and returns a new instance of the Ant task mapped to the
     * specified logical name using the
     * {@link org.apache.cactus.integration.ant.util.AntTaskFactory} set.
     * 
     * @param theName The logical name of the task to create
     * @return A new isntance of the task
     * @see AntTaskFactory#createTask
     */
    protected final Task createAntTask(String theName)
    {
        return this.antTaskFactory.createTask(theName);
    }

    /**
     * Convenience method for creating a new directory inside another one.
     * 
     * @param theParentDir The directory in which the new directory should be
     *        created
     * @param theName The name of the directory to create
     * @return The new directory
     * @throws IOException If the directory could not be created
     */
    protected final File createDirectory(File theParentDir, String theName)
        throws IOException
    {
        File dir = new File(theParentDir, theName);
        dir.mkdirs();
        if (!dir.isDirectory())
        {
            throw new IOException(
                "Couldn't create directory " + dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * Creates the default filter chain that should be applied while copying
     * container configuration files to the temporary directory from which the
     * container is started. The default filter chain replaces all occurences
     * of @cactus.port@ with the TCP port of the container, and all occurences
     * of @cactus.context@ with the web-application's context path (if the
     * deployable file is a web-app).
     * 
     * @return The default filter chain
     */
    protected final FilterChain createFilterChain()
    {
        ReplaceTokens.Token token = null;
        FilterChain filterChain = new FilterChain();

        // Token for the cactus port
        ReplaceTokens replacePort = new ReplaceTokens();
        token = new ReplaceTokens.Token();
        token.setKey("cactus.port");
        token.setValue(String.valueOf(getPort()));
        replacePort.addConfiguredToken(token);
        filterChain.addReplaceTokens(replacePort);

        // Token for the cactus webapp context.
        if (getDeployableFile() != null)
        {
            ReplaceTokens replaceContext = new ReplaceTokens();
            token = new ReplaceTokens.Token();
            token.setKey("cactus.context");
            token.setValue(getDeployableFile().getTestContext());
            replaceContext.addConfiguredToken(token);
            filterChain.addReplaceTokens(replaceContext);
        }
        
        return filterChain;
    }

    /**
     * Clean the temporary directory.
     * 
     * @param theTmpDir the temp directory to clean
     */
    protected void cleanTempDirectory(File theTmpDir)
    {
        // Clean up stuff previously put in the temporary directory
        Delete delete = (Delete) createAntTask("delete");
        FileSet fileSet = new FileSet();
        fileSet.setDir(theTmpDir);
        fileSet.createInclude().setName("**/*");
        delete.addFileset(fileSet);
        delete.setIncludeEmptyDirs(true);
        delete.setFailOnError(false);
        delete.execute();
    }
    
    /**
     * Convenience method that creates a temporary directory or
     * prepares the one passed by the user.
     * 
     * @return The temporary directory
     * @param theCustomTmpDir The user specified custom dir or null if none has
     *        been specified (ie we'll create default one).
     * @param theName The name of the directory relative to the system specific
     *        temporary directory
     */
    protected File setupTempDirectory(File theCustomTmpDir, String theName)
    {
        File tmpDir;
        
        if (theCustomTmpDir == null)
        {
            tmpDir = new File(System.getProperty("java.io.tmpdir"), theName);
        }
        else
        {
            tmpDir = theCustomTmpDir;
        }
        
        if (!tmpDir.exists())
        {
            if (!tmpDir.mkdirs())
            {
                throw new BuildException("Could not create temporary "
                    + "directory [" + tmpDir + "]");
            }
        }

        // make sure we're returning a directory
        if (!tmpDir.isDirectory())
        {
            throw new BuildException("[" + tmpDir + "] is not a directory");
        }

        return tmpDir;
    }
    
    /**
     * Returns the log to use.
     * 
     * @return The log
     */
    protected final Log getLog()
    {
        return this.log;
    }

    /**
     * Returns the web-application archive that is to be deployed to the
     * container.
     * 
     * @return The WAR file  
     */
    protected final DeployableFile getDeployableFile()
    {
        return this.deployableFile;
    }

    // Private Methods ---------------------------------------------------------
        
    /**
     * Tests whether the property necessary to run the tests in the container 
     * has been set.
     * 
     * @return <code>true</code> if the tests should be run in the container,
     *         <code>false</code> otherwise
     */
    private boolean testIfCondition()
    {
        if (ifCondition == null || ifCondition.length() == 0)
        {
            return true;
        }
        
        return (getProject().getProperty(ifCondition) != null);
    }

    /**
     * Tests whether the property specified as the 'unless' condition has not
     * been set.
     * 
     * @return <code>true</code> if the tests should be run in the container,
     *         <code>false</code> otherwise
     */
    private boolean testUnlessCondition()
    {
        if (unlessCondition == null || unlessCondition.length() == 0)
        {
            return true;
        }
        return (getProject().getProperty(unlessCondition) == null);
    }

}
