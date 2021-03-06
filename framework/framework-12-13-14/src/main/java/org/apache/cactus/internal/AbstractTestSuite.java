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
package org.apache.cactus.internal;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.apache.cactus.ServletTestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Test Suite that wraps all the tests of the suite in Cactus Test Case 
 * objects so that pure JUnit tests can be run on the server side.
 *
 * @version $Id: AbstractTestSuite.java 238991 2004-05-22 11:34:50Z vmassol $
 * @since 1.5
 */
public abstract class AbstractTestSuite implements Test
{
    /**
     * Lists of tests to execute (Test objects).
     */
    private Vector tests = new Vector(10);

    /**
     * Name of the current test suite.
     */
    private String name;
    
    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#TestSuite()
     */
    public AbstractTestSuite()
    {
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#TestSuite(Class)
     */
    public AbstractTestSuite(final Class theClass)
    {
        setName(theClass.getName());
        Constructor constructor;
        try
        {
            // Avoid generating multiple error messages
            constructor = getTestConstructor(theClass); 
        }
        catch (NoSuchMethodException e)
        {
            addTest(warning("Class " + theClass.getName()
                + " has no public constructor TestCase(String name)"));
            return;
        }

        if (!Modifier.isPublic(theClass.getModifiers()))
        {
            addTest(warning("Class " + theClass.getName() + " is not public"));
            return;
        }

        Class superClass = theClass;
        Vector names = new Vector();
        while (Test.class.isAssignableFrom(superClass))
        {
            Method[] methods = superClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++)
            {
                addTestMethod(methods[i], names, constructor);
            }
            superClass = superClass.getSuperclass();
        }
        if (this.tests.size() == 0)
        {
            addTest(warning("No tests found in " + theClass.getName()));
        }
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#TestSuite(String)
     */
    public AbstractTestSuite(String theName)
    {
        setName(theName);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#addTest(Test)
     */
    protected void addTest(Test theTest)
    {
        this.tests.addElement(theTest);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#addTestSuite(Class)
     */
    protected void addTestSuite(Class theTestClass)
    {
        addTest(createTestSuite(theTestClass));
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#addTestMethod(Method, Vector, Constructor)
     */
    private void addTestMethod(Method theMethod, Vector theNames, 
        Constructor theConstructor)
    {
        String name = theMethod.getName();
        if (theNames.contains(name))
        {
            return;
        }
        if (isPublicTestMethod(theMethod))
        {
            theNames.addElement(name);

            try
            {
                // Note: We wrap the Test in a Cactus Test Case
                Object constructorInstance;
                if (theConstructor.getParameterTypes().length == 0)
                {
                    constructorInstance = theConstructor.newInstance(
                        new Object[0]);
                    if (constructorInstance instanceof TestCase)
                    {
                        ((TestCase) constructorInstance).setName(name);
                    }
                }
                else
                {
                    constructorInstance = theConstructor.newInstance(
                        new Object[] {name});
                }
                addTest(new ServletTestCase(name, (Test) constructorInstance));
            }
            catch (InstantiationException e)
            {
                addTest(warning("Cannot instantiate test case: " + name
                    + " (" + exceptionToString(e) + ")"));
            }
            catch (InvocationTargetException e)
            {
                addTest(warning("Exception in constructor: " + name + " (" 
                    + exceptionToString(e.getTargetException()) + ")"));
            }
            catch (IllegalAccessException e)
            {
                addTest(warning("Cannot access test case: " + name + " ("
                    + exceptionToString(e) + ")"));
            }
        }
        else
        { 
            // Almost a test method
            if (isTestMethod(theMethod))
            {
                addTest(warning("Test method isn't public: " 
                    + theMethod.getName()));
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#exceptionToString(Throwable)
     */
    private String exceptionToString(Throwable theThrowable)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        theThrowable.printStackTrace(writer);
        return stringWriter.toString();
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#countTestCases()
     */
    public int countTestCases()
    {
        int count = 0;
        for (Enumeration e = tests(); e.hasMoreElements();)
        {
            Test test = (Test) e.nextElement();
            count = count + test.countTestCases();
        }
        return count;
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#isPublicTestMethod(Method)
     */
    private boolean isPublicTestMethod(Method theMethod)
    {
        return isTestMethod(theMethod) 
            && Modifier.isPublic(theMethod.getModifiers());
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#isTestMethod(Method)
     */
    private boolean isTestMethod(Method theMethod)
    {
        String name = theMethod.getName();
        Class[] parameters = theMethod.getParameterTypes();
        Class returnType = theMethod.getReturnType();
        return parameters.length == 0
            && name.startsWith("test")
            && returnType.equals(Void.TYPE);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#run(TestResult)
     */
    public void run(TestResult theResult)
    {
        for (Enumeration e = tests(); e.hasMoreElements();)
        {
            if (theResult.shouldStop())
            {
                break;
            }
            Test test = (Test) e.nextElement();
            runTest(test, theResult);
        }
    }
    
    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#runTest(Test, TestResult)
     */
    protected void runTest(Test theTest, TestResult theResult)
    {
        theTest.run(theResult);
    }
    
    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#testAt(int)
     */
    protected Test testAt(int theIndex)
    {
        return (Test) this.tests.elementAt(theIndex);
    }

    /**
     * Gets a constructor which takes a single String as
     * its argument or a no arg constructor.
     * 
     * @param theClass the class for which to find the constructor
     * @return the valid constructor found
     * @exception NoSuchMethodException if no valid constructor is
     *            found
     */
    protected static Constructor getTestConstructor(Class theClass) 
        throws NoSuchMethodException
    {
        Constructor result;
        try
        {
            result = theClass.getConstructor(new Class[] {String.class});
        }
        catch (NoSuchMethodException e)
        {
            result = theClass.getConstructor(new Class[0]);
        }
        return result; 
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#testCount()
     */
    protected int testCount()
    {
        return this.tests.size();
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#tests()
     */
    protected Enumeration tests()
    {
        return this.tests.elements();
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#toString()
     */
    public String toString()
    {
        if (getName() != null)
        {
            return getName();
        }
        return super.toString();
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#setName(String)
     */
    protected void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#getName()
     */
    protected String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestSuite#warning(String)
     */
    private static Test warning(final String theMessage)
    {
        return new TestCase("warning")
        {
            protected void runTest()
            {
                fail(theMessage);
            }
        };
    }

    /**
     * @param theTestClass the test class containing the tests to be included
     *        in the Cactus Test Suite
     * @return a Cactus Test Suite (ex: ServletTestSuite) initialized with a
     *         test class
     */
    protected abstract Test createTestSuite(Class theTestClass);

    /**
     * @param theName the name of the Cactus Test Case
     * @param theTest the wrapped test
     * @return a Cactus Test Case object initialized with the give name and
     *         wrapped test
     */
    protected abstract Test createCactusTestCase(String theName, Test theTest);
}
