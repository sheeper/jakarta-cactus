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
package org.apache.cactus.integration.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant Task to merge 2 <code>web.xml</code> files.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebMergeTask extends Task
{
    /**
     * Location of the original <code>web.xml</code>
     */
    private File original;  

    /**
     * Location of the overriding <code>web.xml</code>
     */
    private File override;  

    /**
     * Location of the resulting <code>web.xml</code>
     */
    private File output;

    /**
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        if (this.original == null)
        {
            throw new BuildException("The [original] attribute is required");
        }
        if (this.override == null)
        {
            throw new BuildException("The [override] attribute is required");
        }
        if (this.output == null)
        {
            throw new BuildException("The [output] attribute is required");
        }

        WebMerge webMerge = new WebMerge();
        webMerge.setOriginal(this.original);
        webMerge.setOverride(this.override);
        webMerge.setOutput(this.output);
        
        try
        {
            webMerge.transform(); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
                        
            throw new BuildException("Failed to merge [" + this.original
                + "] and [" + this.override + "]", e);
        }
    }

    /**
     * @param theOriginal the original <code>web.xml</code>
     */
    public void setOriginal(File theOriginal)
    {
        this.original = theOriginal;
    }

    /**
     * @param theOverride the override <code>web.xml</code>
     */
    public void setOverride(File theOverride)
    {
        this.override = theOverride;
    }

    /**
     * @param theOutput the resulting <code>web.xml</code>
     */
    public void setOutput(File theOutput)
    {
        this.output = theOutput;
    }
}
