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
package org.apache.cactus.eclipse.runner.launcher;

import org.apache.cactus.eclipse.runner.ui.CactusPreferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchShortcut;

/**
 * Launch shortcut used to start the Cactus launch configuration on the
 * current workbench selection.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @version $Id$
 */
public class CactusLaunchShortcut
    extends JUnitLaunchShortcut
{

    /**
     * @return the Cactus launch configuration type. This method overrides
     *         the one in {@link JUnitLaunchShortcut} so that we can return
     *         a Cactus configuration type and not a JUnit one
     */
    protected ILaunchConfigurationType getJUnitLaunchConfigType()
    {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        String configID = CactusLaunchConfiguration.ID_CACTUS_APPLICATION;
        if (CactusPreferences.getJetty())
        {
            configID =
                JettyCactusLaunchConfiguration.ID_CACTUS_APPLICATION_JETTY;
        }
        return lm.getLaunchConfigurationType(configID);
    }
}
