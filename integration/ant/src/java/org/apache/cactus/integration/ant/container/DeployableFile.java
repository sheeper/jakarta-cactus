/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003-2004 The Apache Software Foundation.  All rights
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

import org.apache.cactus.integration.ant.deployment.WarArchive;

/**
 * Represents a component to deploy in a container. It can either be
 * a WAR or an EAR file. 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface DeployableFile
{
    /**
     * @return the file to deploy in a container (either WAR or EAR)
     */
    File getFile();

    /**
     * Returns whether the deployable file is a web-app archive (WAR).
     * 
     * @return <code>true</code> if the deployable file is a WAR
     */
    boolean isWar();

    /**
     * Returns whether the deployable file is an enterprise application archive
     * (EAR).
     * 
     * @return <code>true</code> if the deployable file is a EAR
     */
    boolean isEar();

    /**
     * @return the WAR deployment descriptor object for the WAR containing
     *         the Cactus Servlet redirector
     */
    WarArchive getWarArchive();
    
    /**
     * @return the webapp context which holds the Cactus tests
     */
    String getTestContext();

    /**
     * @param theTestContext the test context that will be used to test if the
     *        container is started or not
     */
    void setTestContext(String theTestContext);
    
    /**
     * Returns the first URL-pattern to which the Cactus servlet redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return The mapping, or <code>null</code> if the servlet redirector is
     *         not defined or mapped in the descriptor
     */
    String getServletRedirectorMapping();

    /**
     * Returns the first URL-pattern to which the Cactus filter redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return The mapping, or <code>null</code> if the filter redirector is
     *         not defined or mapped in the descriptor
     */
    String getFilterRedirectorMapping();

    /**
     * Returns the first URL-pattern to which the Cactus JSP redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return The mapping, or <code>null</code> if the JSP redirector is
     *         not defined or mapped in the descriptor
     */
    String getJspRedirectorMapping();

    /**
     * Clone the object.
     * @return the object clone
     * @throws CloneNotSupportedException If clone is not supported (duh)
     */
    Object clone() throws CloneNotSupportedException;
}
