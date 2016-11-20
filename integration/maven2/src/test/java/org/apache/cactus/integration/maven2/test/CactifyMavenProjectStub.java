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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
/**
 * A maven project stub for our cactify purposes.
 * 
 * @version $Id: CactifyMavenProjectStub.java 238816 2004-02-29 16:36:46Z ptahchiev$
 */
public class CactifyMavenProjectStub extends MavenProjectStub 
{

    /**
     * The groupId, artifactId and version for our plugin.
     */
    private String groupId, artifactId, version;

    /**
     * The artifact object for our plugin.
     */
    private Artifact artifact;

    /**
     * A set of artifacts we need.
     */
    private Set artifacts;

    /**
     * The model of the pom.
     */
    private Model model;

    /**
     * The base directory for the project.
     */
    private File basedir;

    /**
     * @return the build for the project
     */
    public Build getBuild()
    {
        return model.getBuild();
    }

    /**
     * @return of remote artifact repositories
     */
    public List getRemoteArtifactRepositories()
    {
        ArtifactRepository repository = new DefaultArtifactRepository(
                "central", "file://" 
                + PlexusTestCase.getBasedir() + "/src/test/remote-repo",
                                             new DefaultRepositoryLayout());

        return Collections.singletonList(repository);
    }

    /**
     * Constructor for our stub.
     */
    public CactifyMavenProjectStub()
    {
        groupId = "cactify";
        artifactId = "test-project";
        version = "1.0";
    }

    /**
     * @return a set of dependency artifacts.
     */
    public Set getDependencyArtifacts()
    {
        return Collections.singleton(
            new DefaultArtifact("cactify", "dependency-artifact", 
                    VersionRange.createFromVersion("1.0"),
                    Artifact.SCOPE_COMPILE, "jar", null, 
                    new DefaultArtifactHandler("jar"), false)
            );
    }

    /**
     * @return the base directory as a File.
     */
    public File getBasedir()
    {
        if (basedir == null)
        {
            basedir = new File(PlexusTestCase.getBasedir());
        }

        return basedir;
    }

    /**
     * @return the artifact for the project.
     */
    public Artifact getArtifact()
    {
        if (artifact == null)
        {
            artifact = new ArtifactStub(groupId, artifactId, version, "jar", 
                    Artifact.SCOPE_COMPILE);
        }

        return artifact;
    }

    /**
     * @return the model of the project.
     */
    public Model getModel()
    {
        if (model == null)
        {
            model = new Model();

            model.setProperties(new Properties());

            model.setGroupId(getGroupId());

            model.setArtifactId(getArtifactId());

            model.setVersion(getVersion());

            Build build = new Build();
            build.setFinalName(getArtifactId() + "-" + getVersion());
            model.setBuild(build);
        }

        return model;
    }

    /**
     * @return a set of artifacts.
     */
    public Set getArtifacts()
    {
        if (artifacts == null)
        {
            artifacts = Collections.EMPTY_SET;
        }

        return artifacts;
    }

    /**
     * Setter for the artifacts.
     * @param theArtifacts to set
     */
    public void setArtifacts(Set theArtifacts)
    {
        this.artifacts = theArtifacts;
    }

    /**
     * @return the properties.
     */
    public Properties getProperties()
    {
        return new Properties();
    }

    /**
     * @return the groupId
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * @param theGroupId for the project
     */
    public void setGroupId(String theGroupId)
    {
        this.groupId = theGroupId;
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @param theArtifactId for the project.
     */
    public void setArtifactId(String theArtifactId)
    {
        this.artifactId = theArtifactId;
    }

    /**
     * @return the version of the project
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param theVersion of the project
     */
    public void setVersion(String theVersion)
    {
        this.version = theVersion;
    }
}
