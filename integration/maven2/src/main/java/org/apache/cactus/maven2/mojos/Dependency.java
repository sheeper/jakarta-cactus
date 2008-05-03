package org.apache.cactus.maven2.mojos;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * 
 * Dependency that we use for the lib folder add-on's.
 *
 */
public class Dependency 
extends org.codehaus.cargo.maven2.configuration.Dependency 
{
    public String getDependencyPath(MavenProject project, Log log) throws MojoExecutionException
    {
        String path = getLocation();

        if (path == null)
        {
            if ((getGroupId() == null) || (getArtifactId() == null))
            {
                throw new MojoExecutionException("You must specify a groupId/artifactId or "
                    + "a location that points to a directory or JAR");
            }

            // Default to jar if not type is specified
            if (getType() == null)
            {
                setType("jar");
            }

            path = findArtifactLocation(project.getArtifacts(), log);
        }

        log.debug("Classpath location = [" + new File(path).getPath() + "]");

        return new File(path).getPath();
    }	

}
