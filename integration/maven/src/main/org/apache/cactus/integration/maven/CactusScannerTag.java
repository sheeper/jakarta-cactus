/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.maven;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.tags.ant.TaskSource;
import org.apache.commons.jelly.tags.ant.AntTagLibrary;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Cactus Jelly Tag that scans Ant FileSets and return a list of
 * qualified class name that are Cactus TestCases (i.e.
 * ServletTestCase, JspTestCase or FilterTestCase) or subclasses
 * of Cactus TestCases.
 *
 * Note: This is useful when used with the &lt;junit&gt; Ant
 * task for example, in order to find out the list of tests to
 * execute.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @version $Id$
 */
public class CactusScannerTag extends TagSupport implements TaskSource
{
    private Log log = LogFactory.getLog(CactusScannerTag.class);

    /**
     * The {@link CactusScanner} object that is exposed by this tag
     * to the Jelly script.
     */
    private CactusScanner cactusScanner;

    /**
     * We need to save the fileset as its XML attributes are set after the
     * fileset object is created and thus we need to wait until it is
     * completely initialized before being able to process it using
     * {@link CactusScanner#processFileSet}.
     */
    private FileSet fileset;

    /**
     * Nested &lt;classpath&gt; tag values. This is the classpath that will
     * be used to dynamically load the test classes to decide whether they
     * are Cactus tests or not.
     *
     * Note: There is a bug in Jelly and it does not work yet. ATM you should
     * use the classpathref attribute instead.
     */
    private Path classpath;

    /**
     * Reference to an Ant {@link Path} object containing the classpath.
     * @see #classpath
     */
    private String classpathref;

    /**
     * The Jelly variable (exposed to the Jelly script) that will
     * contain a reference to the {@link CactusScanner} object.
     */
    private String var;

    public CactusScannerTag()
    {
        this.cactusScanner = new CactusScanner();
    }

    /**
     * @see TagSupport#doTag(XMLOutput)
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        this.cactusScanner.setProject(AntTagLibrary.getProject(context));
        this.cactusScanner.clear();

        // run the body first to configure the task via nested tags
        invokeBody(xmlOutput);

        // Process the fileset to extract Cactus test cases. We need to pass
        // the project dependency classpath as the CactusScanner will need
        // to load the cactus test classes to decide whether they are Cactus
        // test case or not and that needs the dependent jars to be in the
        // classpath.
        Path cp = this.classpath;
        if (this.classpathref != null)
        {
            cp = (Path) AntTagLibrary.getProject(
                context).getReference(this.classpathref);
        }

        this.cactusScanner.processFileSet(this.fileset, cp);

        // output the cactusScanner
        if (var == null)
        {
            throw new MissingAttributeException("var");
        }
        context.setVariable(var, cactusScanner);
    }

    /**
     * This method is called internally by Jelly to know on which object to
     * call the {@link TaskSource#setTaskProperty} method.
     *
     * @see TaskSource#getTaskObject()
     */
    public Object getTaskObject()
    {
        return this;
    }

    /**
     * @see TaskSource#setTaskProperty(String, Object)
     */
    public void setTaskProperty(String name, Object value) 
        throws JellyTagException
    {
        try
        {
            BeanUtils.setProperty(this, name, value);
        }
        catch (IllegalAccessException anException)
        {
            throw new JellyTagException(anException);
        }
        catch (InvocationTargetException anException)
        {
            throw new JellyTagException(anException);
        }
        
    }

    /**
     * Adds a set of files (nested fileset attribute). This method is called
     * dynamically by {@link #setTaskProperty}.
     */
    public void addFileset(FileSet set)
    {
        log.debug("Adding fileset [" + set + "]");
        this.fileset = set;
    }

    public Path createClasspath()
    {
        log.debug("Creating classpath");
        if (this.classpath == null)
        {
            this.classpath = new Path(AntTagLibrary.getProject(context));
        }
        return this.classpath.createPath();
    }

    public Path getClasspath()
    {
        return this.classpath;
    }

    public void setClasspath(Path classpath)
    {
        log.debug("Setting classpath [" + classpath + "]");
        if (this.classpath == null)
        {
            this.classpath = classpath;
        } 
        else
        {
            this.classpath.append(classpath);
        }
    }

    public void setClasspathRef(Reference r)
    {
        createClasspath().setRefid(r);
    }

    /**
     * @return the Cactus scanner object
     */
    public CactusScanner getCactusScanner()
    {
        return this.cactusScanner;
    }

    /**
     * Sets the name of the variable exported by this tag
     */
    public void setVar(String var)
    {
        this.var = var;
    }

    public void setClasspathref(String classpathref)
    {
        this.classpathref = classpathref;
    }
}
