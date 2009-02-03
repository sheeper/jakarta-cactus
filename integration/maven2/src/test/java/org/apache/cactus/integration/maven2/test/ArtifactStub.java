/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cactus.integration.maven2.test;

import java.io.File;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Artifact stub to use in the maven2 tests.
 * 
 * @version $Id: ArtifactStub.java 238816 2004-02-29 16:36:46Z ptahchiev$
 */
public class ArtifactStub
    extends DefaultArtifact
{
    /**
     * A constructor for the stub.
     * 
     * @param theGroupId to set
     * @param theArtifactId to set
     * @param theVersion to set
     * @param thePackaging to set
     * @param theScope to set
     */
    public ArtifactStub(String theGroupId, String theArtifactId, 
            String theVersion, String thePackaging, String theScope)
    {
        this(theGroupId, theArtifactId, theVersion, thePackaging, 
                null, theScope);
    }

    /**
     * Another constructor for the stub.
     * 
     * @param theGroupId to use
     * @param theArtifactId to use
     * @param theVersion to use
     * @param thePackaging to use
     * @param theClassifier to use
     * @param theScope to use
     */
    public ArtifactStub(String theGroupId, String theArtifactId, 
            String theVersion, String thePackaging, String theClassifier, 
            String theScope)
    {
        super(theGroupId, theArtifactId, 
            VersionRange.createFromVersion(theVersion), theScope, thePackaging,
            theClassifier, new DefaultArtifactHandler(thePackaging), false);
    }

    /**
     * @return File the file from the local repository 
     * constructed with our data.
     */
    public File getFile()
    {
        return new File(PlexusTestCase.getBasedir() + "/target/local-repo", 
            getArtifactId() + "-" + getVersion() + "." + getType())
        {
            public long lastModified()
            {
                return System.currentTimeMillis();
            }
        };
    }
}
