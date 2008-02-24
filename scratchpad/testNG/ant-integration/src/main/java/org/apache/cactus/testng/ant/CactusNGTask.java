/**
 * 
 */
package java.org.apache.cactus.testng.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.cactus.integration.ant.CargoElement;
import org.apache.cactus.integration.ant.ContainerSet;
import org.apache.cactus.integration.ant.container.ContainerRunner;
import org.apache.cactus.integration.ant.container.ContainerWrapper;
import org.apache.cactus.integration.ant.deployment.DeployableFile;
import org.apache.cactus.integration.ant.deployment.EarParser;
import org.apache.cactus.integration.ant.deployment.WarParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Environment.Variable;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.util.log.AntLogger;
import org.testng.TestNGAntTask;

/**
 * @author Petar
 *
 */
public class CactusNGTask extends TestNGAntTask {
	
    // Instance Variables ------------------------------------------------------
    /**
     * The nested containerset element.
     */
    private ContainerSet containerSet;
    
    

    /**
     * The archive that contains the enterprise application that should be
     * tested.
     */
    private File earFile;

    /**
     * The archive that contains the web-app that is ready to be tested.
     */
    private File warFile;

    /**
     * System properties that will be set in the container JVM.
     */
    private Map systemProperties = new HashMap();

    /**
     * Additional classpath entries for the classpath that will be used to 
     * start the containers.
     */
    private Path containerClasspath;
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @throws Exception If the constructor of JUnitTask throws an exception
     */
    public CactusNGTask() throws Exception
    {
        // TODO: Fix comment for this constructor as it doesn't seem quite 
        // right. Explain why we don't call the super constructor?
    }
    
    // Public Methods ----------------------------------------------------------
    

    /**
     * @see org.apache.tools.ant.Task#init()
     */
    public void init()
    {
        super.init();
        
        setClasspathRef(new Reference(getProject(), "/org/aspectj/lang/JoinPoint.class"));
        setClasspathRef(new Reference(getProject(), "/org/apache/cactus/ServletTestCase.class"));
        setClasspathRef(new Reference(getProject(), 
            "/org/apache/cactus/integration/ant/CactusTask.class"));
        setClasspathRef(new Reference(getProject(), "/org/apache/commons/logging/Log.class"));
        setClasspathRef(new Reference(getProject(), "/org/apache/commons/httpclient/HttpClient.class"));
    }
    /**
     * Returns a path of of testNG tests to execute; 
     * @return <code>Path</code>
     */
    public String[] getClassPathForExecution() {
    	return super.getJavaCommand().getClasspath().translatePath(getProject(), System.getProperty("path.separator"));
    }
    
    /**
     * {@inheritDoc}
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        log("cactus tag is under major change " 
            + " consider to use cactustest instead" , Project.MSG_INFO);
        if ((this.warFile == null) && (this.earFile == null))
        {
            throw new BuildException("You must specify either the [warfile] or "
                + "the [earfile] attribute");
        }

        if ((this.warFile != null) && (this.earFile != null))
        {
            throw new BuildException("You must specify either the [warfile] or "
                + "the [earfile] attribute but not both");
        }

        // Parse deployment descriptors for WAR or EAR files
        DeployableFile deployableFile;
        if (this.warFile != null)
        {
            deployableFile = WarParser.parse(this.warFile);
        }
        else 
        {
            deployableFile = EarParser.parse(this.earFile);
        } 

        addRedirectorNameProperties(deployableFile);
        
        if (containerSet.getCargos() == null)
        {
            log("No cargo configurations specified, tests will run locally",
                Project.MSG_VERBOSE);
            super.execute();
        }
        else
        {
            CargoElement[] cargoElements = this.containerSet.getCargos();
            Variable contextUrl = new Variable();
            contextUrl.setKey("cactus.contextURL");

            addSysproperty(contextUrl);

            for (int i = 0; i < cargoElements.length; i++)
            {
                CargoElement element = (CargoElement) cargoElements[i];
                Container container = element.getCargoContainer();
                ContainerWrapper wrapper = new ContainerWrapper(container);
                wrapper.setLogger(new AntLogger(this));

                // Clone the DeployableFile instance as each container can
                // override default deployment properties (e.g. port, context
                // root, etc).
                DeployableFile thisDeployable = null;
                try
                {
                    thisDeployable = (DeployableFile) deployableFile.clone();
                }
                catch (CloneNotSupportedException e)
                {
                    throw new BuildException(e);
                }

                // Allow the container to override the default test context. 
                // This is to support container extensions to the web.xml file.
                // Most containers allow defining the root context in these
                // extensions.
                wrapper.setSystemProperties(this.systemProperties);

                // Add extra classpath entries
                wrapper.setContainerClasspath(this.containerClasspath);
                
                if (wrapper.isEnabled())
                {
                    wrapper.init();
                    log("--------------------------------------------------"
                        + "---------------", Project.MSG_INFO);
                    log("Running tests against " + wrapper.getName()
                        + " @ " + wrapper.getBaseURL(),
                        Project.MSG_INFO);
                    log("--------------------------------------------------"
                        + "---------------", Project.MSG_INFO);
                    contextUrl.setValue(wrapper.getBaseURL() + "/"
                        + thisDeployable.getTestContext());
                    executeInContainer(wrapper, thisDeployable); 
                }
            }
        }
    }
    
    // Private Methods ---------------------------------------------------------

    /**
     * Adds a Cactus system property for the client side JVM.
     * 
     * @param theKey The property name
     * @param theValue The property value
     */
    private void addCactusClientProperty(String theKey, String theValue)
    {
        log("Adding Cactus client system property [" + theKey 
            + "] with value [" + theValue + "]", Project.MSG_VERBOSE);
        Variable sysProperty = new Variable();
        sysProperty.setKey(theKey);
        sysProperty.setValue(theValue);
        super.addSysproperty(sysProperty);
    }

    /**
     * Adds a Cactus system property for the server side JVM.
     * 
     * @param theProperty The system property to set in the container JVM
     */
    private void addCactusServerProperty(Variable theProperty)
    {
        log("Adding Cactus server system property [" 
            + theProperty.getKey() + "] with value [" 
            + theProperty.getValue() + "]", Project.MSG_VERBOSE);
        this.systemProperties.put(theProperty.getKey(), theProperty.getValue());
    }

    /**
     * Adds a Cactus system property for the server side JVM.
     * 
     * @param theKey The property name
     * @param theValue The property value
     */
    private void addCactusServerProperty(String theKey, String theValue)
    {
        Variable property = new Variable();
        property.setKey(theKey);
        property.setValue(theValue);
        addCactusServerProperty(property);
    }
    
    /**
     * Extracts the redirector mappings from the deployment descriptor and sets 
     * the corresponding system properties.
     * 
     * @param theFile The file to deploy in the container
     */
    private void addRedirectorNameProperties(DeployableFile theFile)
    {
        String filterRedirectorMapping = 
            theFile.getFilterRedirectorMapping();
        if (filterRedirectorMapping != null)
        {
            addCactusClientProperty("cactus.filterRedirectorName",
                filterRedirectorMapping.substring(1));
        }
        else
        {
            log("No mapping of the filter redirector found",
                Project.MSG_VERBOSE);
        }

        String jspRedirectorMapping = 
            theFile.getJspRedirectorMapping();
        if (jspRedirectorMapping != null)
        {
            addCactusClientProperty("cactus.jspRedirectorName",
                jspRedirectorMapping.substring(1));
        }
        else
        {
            log("No mapping of the JSP redirector found",
                Project.MSG_VERBOSE);
        }

        String servletRedirectorMapping = 
            theFile.getServletRedirectorMapping();
        if (servletRedirectorMapping != null)
        {
            addCactusClientProperty("cactus.servletRedirectorName",
                servletRedirectorMapping.substring(1));
        }
        else
        {
            throw new BuildException("The WAR has not been cactified");
        }
    }
    
    /**
     * Executes the unit tests in the given container.
     * 
     * @param theWrapper The containerWrapper to run the tests against
     * @param theFile the file to deploy in the container
     */
    private void executeInContainer(ContainerWrapper theWrapper, 
        DeployableFile theFile)
    {
        log("Starting up container", Project.MSG_VERBOSE);
        ContainerRunner runner = new ContainerRunner(theWrapper);
        runner.setLogger(new AntLogger(getProject()));
        try
        {
            URL url = new URL(theWrapper.getBaseURL() + "/"
                + theFile.getTestContext() 
                + theFile.getServletRedirectorMapping()
                + "?Cactus_Service=RUN_TEST");
            runner.setURL(url);
         
            runner.startUpContainer();
            log("Server name retrieved from 'Server' HTTP header: ["
                + runner.getServerName() + "]", Project.MSG_VERBOSE);
            try
            {
                String[] tests = getClassPathForExecution(); 
                //while (tests.hasMoreElements())
                //{
                    //JUnitTest test = (JUnitTest) tests.nextElement();
                    //if (test.shouldRun(getProject())
                     //&& !theWrapper.isExcluded(test.getName()))
                    //{
                     //   if (theWrapper.getToDir() != null)
                     //  {
                     //       test.setTodir(theWrapper.getToDir());
                     //   }
                        super.execute();
                //    }
                //}
            }
            finally
            {
                log("Shutting down container", Project.MSG_VERBOSE);
                runner.shutDownContainer();
                log("Container shut down", Project.MSG_VERBOSE);
            }
        }
        catch (MalformedURLException mue)
        {
            throw new BuildException("Malformed test URL", mue);
        }
    }
}
