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
package org.apache.cactus.eclipse.ant;

import org.apache.cactus.eclipse.containers.ant.GenericAntProvider;
import org.apache.cactus.eclipse.launcher.CactusLaunchShortcut;
import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.apache.tools.ant.Task;
import org.eclipse.swt.widgets.Display;

/**
 * This Ant task is used for running tests between container startup and
 * shutdown.
 *
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 *
 * @version $Id$
 */

public class EclipseRunTests extends Task implements Runnable
{
    /**
     * Indicates that tests are finished, meaning that the task can terminate.
     */
    private boolean isFinished = false;
    /**
     * Launches the Junit tests in Eclipse
     */
    public void execute()
    {
        Display.getDefault().asyncExec(this);
        while (!isFinished)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // Do nothing
            }
        }
    }

    /**
     * This method notifies the instance that tests are finished and that
     * it can terminate.
     */
    public void finish()
    {
        isFinished = true;
    }

    /**
     * Launches Cactus tests.
     */
    public void run()
    {
        CactusLaunchShortcut launchShortcut =
            CactusPlugin.getDefault().getCactusLaunchShortcut();
        GenericAntProvider antProvider =
            (GenericAntProvider) launchShortcut.getContainerProvider();
        antProvider.setEclipseRunner(this);
        launchShortcut.launchJunitTests();
    }
}
