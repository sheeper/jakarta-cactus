package org.apache.cactus.eclipse.containers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Julien Ruaux
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Tomcat40AntContainerProvider implements IContainerProvider
{
    /**
     * Properties passed to the ant script. 
     */
    private String[] antArguments;

    /**
     * Location of the build.xml file. 
     */
    private String buildFileLocation;

    /**
     * Constructor for Tomcat40AntContainerProvider. 
     */
    public Tomcat40AntContainerProvider()
    {
//        URL buildFileURL =
//            CactusPlugin.getDefault().find(new Path("ant/build-share.xml"));
//        runner.setBuildFileLocation(buildFileURL.getPath());
        buildFileLocation =
            "D:/dev/eclipse/workspace/Eclipse-Plugin/ant/build/build.xml";
        // Properties passed as arguments to Ant :
        // - target.dir (dir where the build will be done
        //         (e.g. the war file) (sort of temp dir))
        // - base.dir (dir where the build (ant scripts) and conf dirs are)
        // - src.dir (java cactus test files)
        Vector arguments = new Vector();
        
        String baseDirectory = "D:/dev/cactus/jakarta-cactus-13-1.4b1/lib/";
        
        arguments.add("-Dtarget.dir=D:/temp/antTemp");
        arguments.add("-Dtest.port=8081");
        arguments.add("-Dbase.dir=D:/dev/eclipse/workspace/Eclipse-Plugin/ant");
        arguments.add("-Dsrc.dir=D:/dev/eclipse/runtime-workspace/cactus-test");
        arguments.add("-Dtomcat.home.40=D:/dev/jakarta-tomcat-4.1.12");

        arguments.add("-Dcactus.framework.jar=" + baseDirectory 
            + "cactus-1.4b1.jar");
        arguments.add("-Dcactus.ant.jar=" + baseDirectory
            + "cactus-ant-1.4b1.jar");
        arguments.add("-Dservlet.jar=" + baseDirectory    
            + "servletapi-2.3.jar");
        arguments.add("-Daspectjrt.jar=" + baseDirectory
            + "aspectjrt-1.0.5.jar");
        arguments.add("-Dcommons.logging.jar=" + baseDirectory
            + "commons-logging-1.0.jar");
        arguments.add("-Dlog4j.jar=" + baseDirectory 
            + "log4j-1.2.5.jar");
        arguments.add("-Dcommons.httpclient.jar=" + baseDirectory
            + "commons-httpclient-2.0alpha1-20020720.jar");
        arguments.add("-Djunit.jar=" + baseDirectory
            + "junit-3.7.jar");
        arguments.add("-Dhttpunit.jar=" + baseDirectory
            + "httpunit-1.4.1.jar");

        antArguments = (String[]) arguments.toArray(new String[0]);
    }

    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#deploy()
     */
    public void deploy() throws CoreException
    {
        String[] targets = { "prepare.test.tomcat.40" };
        createAntRunner(targets).run();
    }

    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#start()
     */
    public void start() throws CoreException
    {
        String[] targets = { "start.tomcat.40" };
        AntRunner runner = createAntRunner(targets);
        StartServerHelper startHelper = new StartServerHelper(runner);
        URL testURL = null;
        try
        {
            testURL = new URL("http://localhost:8081/test/ServletRedirector"
                + "?Cactus_Service=RUN_TEST");
        }
        catch (MalformedURLException e)
        {
            //TODO: Handle exception
        }
        startHelper.setTestURL(testURL);
        startHelper.execute();
    }
    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#undeploy()
     */
    public void undeploy() throws CoreException
    {
        String[] targets = { "clean" };
        createAntRunner(targets).run();
    }

    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#stop()
     */
    public void stop() throws CoreException
    {
        String[] targets = { "stop.tomcat.40" };
        createAntRunner(targets).run();
    }

    /**
     * returns an AntRunner for this provider.
     * @param theTargets the ant target to be called
     * @return the AntRunner for the script
     */
    private AntRunner createAntRunner(String[] theTargets)
    {
        AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFileLocation);
        runner.setArguments(antArguments);
        runner.setExecutionTargets(theTargets);
        return runner;
    }

}
