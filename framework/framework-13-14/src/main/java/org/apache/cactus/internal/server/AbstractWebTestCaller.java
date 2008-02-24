/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus.internal.server;

import java.io.IOException;
import java.io.Writer;

import java.lang.reflect.Constructor;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.internal.CactusTestCase;
import org.apache.cactus.internal.HttpServiceDefinition;
import org.apache.cactus.internal.ServiceEnumeration;
import org.apache.cactus.internal.WebTestResult;
import org.apache.cactus.internal.configuration.Version;
import org.apache.cactus.internal.util.ClassLoaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method. This class
 * provides a common abstraction for all test web requests.
 *
 * @version $Id: AbstractWebTestCaller.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public abstract class AbstractWebTestCaller
{
    /**
     * Name of the attribute in the <code>application</code> scope that will
     * hold the results of the test.
     */
    protected static final String TEST_RESULTS = 
        "ServletTestRedirector_TestResults";

    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(AbstractWebTestCaller.class);

    /**
     * The implicit objects (which will be used to set the test case fields
     * in the <code>setTesCaseFields</code> method.
     */
    protected WebImplicitObjects webImplicitObjects;

    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public AbstractWebTestCaller(WebImplicitObjects theObjects)
    {
        this.webImplicitObjects = theObjects;
    }

    /**
     * Sets the implicit object in the test case class.
     *
     * @param theTestCase the instance of the test case class on which the
     *        class variable (implicit objects) should be set
     * @exception Exception if an errors occurs when setting the implicit
     *            objects
     */
    protected abstract void setTestCaseFields(TestCase theTestCase) 
        throws Exception;

    /**
     * @return a <code>Writer</code> object that will be used to return the
     *         test result to the client side.
     * @exception IOException if an error occurs when retrieving the writer
     */
    protected abstract Writer getResponseWriter() throws IOException;

    /**
     * Calls a test method. The parameters needed to call this method are found
     * in the HTTP request. Save the results in the <code>application</code>
     * scope so that the Get Test Result service can find them.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doTest() throws ServletException
    {
        WebTestResult result = null;

        try
        {
            // Create an instance of the test class
            TestCase testInstance = getTestClassInstance(
                getTestClassName(), getWrappedTestClassName(), 
                getTestMethodName());

            // Set its fields (implicit objects)
            setTestCaseFields(testInstance);

            // Call it's method corresponding to the current test case
            if (testInstance instanceof CactusTestCase)
            {
                ((CactusTestCase) testInstance).runBareServer();                
                
            }
            else
            {
                testInstance.runBare();                
            }

            // Return an instance of <code>WebTestResult</code> with a
            // positive result.
            result = new WebTestResult();
        }
        catch (Throwable e)
        {
            // An error occurred, return an instance of
            // <code>WebTestResult</code> with an exception.
            result = new WebTestResult(e);
        }

        LOGGER.debug("Test result : [" + result + "]");


        // Set the test result.
        this.webImplicitObjects.getServletContext()
            .setAttribute(TEST_RESULTS, result);

        LOGGER.debug("Result saved in context scope");
    }

    /**
     * Return the last test results in the HTTP response.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doGetResults() throws ServletException
    {
        // One could think there is a potential risk that the client side of
        // Cactus will request the result before it has been written to the
        // context scope as the HTTP request will not block in some containers.
        // However this will not happen because on the client side, once the
        // first request is done to execute the test, all the result is read
        // by the AutoReadHttpURLConnection class, thus ensuring that the
        // request is fully finished and the result has been committed ...
        WebTestResult result = (WebTestResult) (this.webImplicitObjects
            .getServletContext().getAttribute(TEST_RESULTS));

        // It can happen that the result has not been written in the Servlet
        // context. This could happen for example when using a load-balancer
        // which would direct the second Cactus HTTP connection to another
        // instance. In that case, we throw an error.
        if (result == null)
        {
            String message = "Error getting test result. This could happen "
                + "for example if you're using a load-balancer. Please disable "
                + "it before running Cactus tests.";

            LOGGER.error(message);
            throw new ServletException(message);
        }       

        LOGGER.debug("Test Result = [" + result + "]");

        // Write back the results to the outgoing stream as an XML string.

        // Use UTF-8 to transfer the result back
        webImplicitObjects.getHttpServletResponse().setContentType(
            "text/xml; charset=UTF-8");

        try
        {
            Writer writer = getResponseWriter();

            writer.write(result.toXml());
            writer.close();
        }
        catch (IOException e)
        {
            String message = "Error writing WebTestResult instance to output "
                + "stream";

            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }
    }

    /**
     * Run the connection test between client and server. This is just to
     * ensure that configuration is set up correctly.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doRunTest() throws ServletException
    {
        // Do not return any http response (not needed). It is enough to
        // know this point has been reached ... it means the connection has
        // been established !
    }

    /**
     * Return the cactus version. This is to make sure both the client side
     * and server side are using the same version.
     *  
     * @exception ServletException if an unexpected error occurred
     */    
    public void doGetVersion() throws ServletException
    {
        try
        {
            Writer writer = getResponseWriter();
            writer.write(Version.VERSION);
            writer.close();
        }
        catch (IOException e)
        {
            String message = "Error writing HTTP response back to client "
                + "for service [" + ServiceEnumeration.GET_VERSION_SERVICE
                + "]";

            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }        
    }
    
    /**
     * Create an HTTP Session and returns the response that contains the
     * HTTP session as a cookie (unless URL rewriting is used in which
     * case the jsesssionid cookie is not returned).
     * 
     * @exception ServletException if an unexpected error occurred
     */
    public void doCreateSession() throws ServletException
    {
        // Create an HTTP session
        this.webImplicitObjects.getHttpServletRequest().getSession(true);

        try
        {
            Writer writer = getResponseWriter();
            writer.close();
        }
        catch (IOException e)
        {
            String message = "Error writing HTTP response back to client "
                + "for service [" + ServiceEnumeration.CREATE_SESSION_SERVICE
                + "]";

            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }
    }

    /**
     * @return the class to test class name, extracted from the HTTP request
     * @exception ServletException if the class name of the test case is missing
     *            from the HTTP request
     */
    protected String getTestClassName() throws ServletException
    {
        String queryString = this.webImplicitObjects.getHttpServletRequest()
            .getQueryString();
        String className = ServletUtil.getQueryStringParameter(queryString, 
            HttpServiceDefinition.CLASS_NAME_PARAM);

        if (className == null)
        {
            String message = "Missing class name parameter ["
                + HttpServiceDefinition.CLASS_NAME_PARAM
                + "] in HTTP request.";

            LOGGER.error(message);
            throw new ServletException(message);
        }

        LOGGER.debug("Class to call = [" + className + "]");

        return className;
    }

    /**
     * @return the optional test class that is wrapped by a Cactus test case, 
     *         extracted from the HTTP request
     * @exception ServletException if the wrapped class name is missing from 
     *            the HTTP request
     */
    protected String getWrappedTestClassName() throws ServletException
    {
        String queryString = this.webImplicitObjects.getHttpServletRequest()
            .getQueryString();
        String className = ServletUtil.getQueryStringParameter(queryString, 
            HttpServiceDefinition.WRAPPED_CLASS_NAME_PARAM);

        if (className == null)
        {
            LOGGER.debug("No wrapped test class");
        } 
        else
        { 
            LOGGER.debug("Wrapped test class = [" + className + "]");
        }

        return className;
    }

    /**
     * @return the class method to call for the current test case, extracted
     *         from the HTTP request
     * @exception ServletException if the method name of the test case is
     *            missing from the HTTP request
     */
    protected String getTestMethodName() throws ServletException
    {
        String queryString = this.webImplicitObjects.getHttpServletRequest()
            .getQueryString();
        String methodName = ServletUtil.getQueryStringParameter(queryString, 
            HttpServiceDefinition.METHOD_NAME_PARAM);

        if (methodName == null)
        {
            String message = "Missing method name parameter ["
                + HttpServiceDefinition.METHOD_NAME_PARAM
                + "] in HTTP request.";

            LOGGER.error(message);
            throw new ServletException(message);
        }

        LOGGER.debug("Method to call = " + methodName);

        return methodName;
    }

    /**
     * @return true if the auto session flag for the Session can be found in
     *         the HTTP request
     */
    protected boolean isAutoSession()
    {
        String queryString = this.webImplicitObjects.getHttpServletRequest()
            .getQueryString();
        String autoSession = ServletUtil.getQueryStringParameter(queryString, 
            HttpServiceDefinition.AUTOSESSION_NAME_PARAM);

        boolean isAutomaticSession = 
            Boolean.valueOf(autoSession).booleanValue();

        LOGGER.debug("Auto session is " + isAutomaticSession);

        return isAutomaticSession;
    }

    /**
     * @param theClassName the name of the test class
     * @param theWrappedClassName the name of the wrapped test class. Can be
     *        null if there is none
     * @param theTestCaseName the name of the current test case
     * @return an instance of the test class to call
     * @exception ServletException if the test case instance for the current
     *            test fails to be instanciated (for example if some
     *            information is missing from the HTTP request)
     */
    protected TestCase getTestClassInstance(
        String theClassName, String theWrappedClassName, 
        String theTestCaseName) throws ServletException
    {
        // Get the class to call and build an instance of it.
        Class testClass = getTestClassClass(theClassName);
        TestCase testInstance = null;
        Constructor constructor;
        
        try
        {
            if (theWrappedClassName == null)
            {
                constructor = getTestClassConstructor(testClass); 

                if (constructor.getParameterTypes().length == 0)
                {
                    testInstance = (TestCase) constructor.newInstance(
                        new Object[0]);
                    ((TestCase) testInstance).setName(theTestCaseName);
                }
                else
                {
                    testInstance = (TestCase) constructor.newInstance(
                        new Object[] {theTestCaseName});                
                }
            }
            else
            {
                Class wrappedTestClass = 
                    getTestClassClass(theWrappedClassName);
                Constructor wrappedConstructor =
                    getTestClassConstructor(wrappedTestClass);

                TestCase wrappedTestInstance;
                if (wrappedConstructor.getParameterTypes().length == 0)
                {
                    wrappedTestInstance = 
                        (TestCase) wrappedConstructor.newInstance(
                        new Object[0]);
                    wrappedTestInstance.setName(theTestCaseName);
                }
                else
                {
                    wrappedTestInstance = 
                        (TestCase) wrappedConstructor.newInstance(
                        new Object[] {theTestCaseName});
                }

                constructor = testClass.getConstructor(
                    new Class[] {String.class, Test.class});

                testInstance = 
                    (TestCase) constructor.newInstance(
                    new Object[] {theTestCaseName, wrappedTestInstance});
            }
        }
        catch (Exception e)
        {
            String message = "Error instantiating class [" + theClassName + "(["
                + theTestCaseName + "], [" + theWrappedClassName + "])]";

            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }

        return testInstance;
    }

    /**
     * @param theTestClass the test class for which we want to find the
     *        constructor
     * @return the availble constructor for the test class
     * @throws NoSuchMethodException if no suitable constructor is found
     */
    private Constructor getTestClassConstructor(Class theTestClass)
        throws NoSuchMethodException
    {
        Constructor constructor;
        try 
        {
            constructor = theTestClass.getConstructor(
                new Class[] {String.class});         
        }
        catch (NoSuchMethodException e)
        {
            constructor = theTestClass.getConstructor(new Class[0]);
        }
        return constructor;        
    }

    /**
     * @param theClassName the name of the test class
     * @return the class object the test class to call
     * @exception ServletException if the class of the current test case
     *            cannot be loaded in memory (i.e. it is not in the
     *            classpath)
     */
    protected Class getTestClassClass(String theClassName)
        throws ServletException
    {
        // Get the class to call and build an instance of it.
        Class testClass = null;

        try
        {
            testClass = ClassLoaderUtils.loadClass(theClassName, 
                this.getClass());
        }
        catch (Exception e)
        {
            String message = "Error finding class [" + theClassName
                + "] using both the Context classloader and the webapp "
                + "classloader. Possible causes include:\r\n";

            message += ("\t- Your webapp does not include your test " 
                + "classes,\r\n");
            message += ("\t- The cactus.jar is not located in your " 
                + "WEB-INF/lib directory and your Container has not set the " 
                + "Context classloader to point to the webapp one");

            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }

        return testClass;
    }

}
