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
package org.apache.cactus.integration.ant.container;

import java.io.File;
import java.io.IOException;

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
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Abstract base class for supporting specific containers as nested elements in
 * the {@link org.apache.cactus.integration.ant.CactusTask}.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
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
     * The web or application archive that should be deployed to the container.
     */
    private File deployableFile;

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

    // Public Methods ----------------------------------------------------------

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
    public void setAntTaskFactory(AntTaskFactory theFactory)
    {
        this.antTaskFactory = theFactory;
    }

    /**
     * @see Container#setDeployableFile
     */
    public final void setDeployableFile(File theDeployableFile)
    {
        this.deployableFile = theDeployableFile;
    }

    /**
     * @see Container#setLog
     */
    public void setLog(Log theLog)
    {
        this.log = theLog;
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
    protected FilterChain createFilterChain()
    {
        ReplaceTokens.Token token = null;
        FilterChain filterChain = new FilterChain();
        ReplaceTokens replacePort = new ReplaceTokens();
        token = new ReplaceTokens.Token();
        token.setKey("cactus.port");
        token.setValue(String.valueOf(getPort()));
        replacePort.addConfiguredToken(token);
        filterChain.addReplaceTokens(replacePort);
        if (isWar(getDeployableFile()))
        {
            ReplaceTokens replaceContext = new ReplaceTokens();
            token = new ReplaceTokens.Token();
            token.setKey("cactus.context");
            String contextPath = getDeployableFile().getName();
            contextPath = contextPath.substring(0, contextPath.length() - 4); 
            token.setValue(contextPath);
            replaceContext.addConfiguredToken(token);
            filterChain.addReplaceTokens(replaceContext);
        }
        return filterChain;
    }

    /**
     * Convenience method that creates a temporary directory.
     * 
     * @param theName The name of the directory relative to the system specific
     *        temporary directory
     * @return The temporary directory
     */
    protected final File createTempDirectory(String theName)
    {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), theName);
        if (!tmpDir.exists())
        {
            if (!tmpDir.mkdirs())
            {
                throw new BuildException("Could not create temporary "
                    + "directory " + tmpDir);
            }
        }
        else
        {
            // Clean up stuff previously put in the temporary directory
            Delete delete = (Delete) createAntTask("delete");
            FileSet fileSet = new FileSet();
            fileSet.setDir(tmpDir);
            fileSet.createInclude().setName("**/*");
            delete.addFileset(fileSet);
            delete.setIncludeEmptyDirs(true);
            delete.execute();
        }
        // make sure we're returning a directory
        if (!tmpDir.isDirectory())
        {
            throw new BuildException(tmpDir + " is not a directory");
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
    protected final File getDeployableFile()
    {
        return this.deployableFile;
    }

    /**
     * Returns whether the deployable file is an enterprise application archive
     * (EAR).
     * 
     * @param theDeployableFile The deployable file to check
     * @return <code>true</code> if the deployable file is a EAR
     */
    protected final boolean isEar(File theDeployableFile)
    {
        return theDeployableFile.getName().toLowerCase().endsWith(".ear");
    }

    /**
     * Returns whether the deployable file is a web-app archive (WAR).
     * 
     * @param theDeployableFile The deployable file to check
     * @return <code>true</code> if the deployable file is a WAR
     */
    protected final boolean isWar(File theDeployableFile)
    {
        return theDeployableFile.getName().toLowerCase().endsWith(".war");
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
