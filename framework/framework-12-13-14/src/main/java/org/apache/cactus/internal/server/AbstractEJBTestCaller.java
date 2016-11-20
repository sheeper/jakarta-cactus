/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cactus.internal.AbstractCactusTestCase;
import org.apache.cactus.internal.EJBTestResult;
import org.apache.cactus.internal.HttpServiceDefinition;
import org.apache.cactus.internal.util.ClassLoaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;

/**
 * Prototype of EJBRedirector for Cactus.
 * 
 * @version $Id$
 */
public abstract class AbstractEJBTestCaller 
{

    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(AbstractWebTestCaller.class);
    
    /**
     * The ejb implicitObjects instance.
     */
    protected EJBImplicitObjects objects;

    /**
     * A constructor for the class.
     * @param theObjects parameter
     */
    public AbstractEJBTestCaller(EJBImplicitObjects theObjects) 
    {
        this.objects = theObjects;
    }
    
    /**
     * Setter method to set the test-case fields.
     * 
     * @param theTestCase parameter
     * @throws Exception in case an error occurs.
     */
    protected abstract void setTestCaseFields(AbstractCactusTestCase 
            theTestCase) throws Exception;

    /**
     * The "main" method of the test-case.
     * @throws Exception in case an error occurs
     */
    public void doTest() throws Exception 
    {
        EJBTestResult result = null;

        try 
        {

            // Create an instance of the test class
            AbstractCactusTestCase testInstance =
                getTestClassInstance(getTestClassName(), getTestMethodName());
            
            LOGGER.debug("CLASS NAME " + getTestClassName());
            LOGGER.debug("METHOD NAME " + getTestMethodName());
            // Set its fields (implicit objects)
            setTestCaseFields(testInstance);

            // Call it's method corresponding to the current test case
            testInstance.runBareServer();

            // Return an instance of <code>WebTestResult</code> with a
            // positive result.
            result = new EJBTestResult();

        } 
        catch (Throwable e) 
        {
            // An error occurred, return an instance of
            // <code>WebTestResult</code> with an exception.
            result = new EJBTestResult(e);
        }
        LOGGER.info("***********************************************");
        LOGGER.info("Test result : [" + result + "]");
        LOGGER.info("***********************************************");
    }
    
    /**
     * Getter method to return the name of the class being tested.
     * 
     * @return the class name
     */
    protected String getTestClassName() 
    {
        return this.objects.getEJBRequest().getClassName(
            HttpServiceDefinition.CLASS_NAME_PARAM);
    }

    /**
     * Getter method to return the name of the test-method being executed.
     * 
     * @return the method-name
     * @throws Exception in case an error occurs
     */
    protected String getTestMethodName() throws Exception 
    {
        return this.objects.getEJBRequest().getMethodName(
            HttpServiceDefinition.METHOD_NAME_PARAM);
    }

    /**
     * A method to return an instance of the test-class.
     * @param theClassName parameter
     * @param theTestCaseName parameter
     * @return the cactus test-case
     * @throws Exception in case an error occurs
     */
    protected AbstractCactusTestCase getTestClassInstance(
        String theClassName,
        String theTestCaseName)
        throws Exception 
    {
        // Get the class to call and build an instance of it.
        Class testClass = getTestClassClass(theClassName);
        AbstractCactusTestCase testInstance = null;
        try 
        {
            Constructor constructor =
                testClass.getConstructor(new Class[] {String.class});
            testInstance =
                (AbstractCactusTestCase) constructor.newInstance(
                    new Object[] {theTestCaseName});
        } 
        catch (Exception e) 
        {
            throw new Exception(e);
        }

        return testInstance;
    }

    /**
     * @param theClassName the name of the test class
     * @return the class object the test class to call
     * @exception Exception in case an error occurs
     */
    protected Class getTestClassClass(String theClassName) throws Exception 
    {
        // Get the class to call and build an instance of it.
        Class testClass = null;
        try 
        {
            testClass =
                ClassLoaderUtils.loadClass(theClassName, this.getClass());
        } 
        catch (Exception e) 
        {
            throw new Exception(e);
        }

        return testClass;
    }
}
