/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
