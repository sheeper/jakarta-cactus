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
package org.apache.cactus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Test Suite that wraps all the tests of the suite in Cactus Test Case 
 * objects so that pure JUnit tests can be run on the server side.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @since 1.5
 */
public abstract class AbstractTestSuite implements Test
{
    /**
     * Lists of tests to execute (Test objects)
     */
    private Vector tests = new Vector(10);

    /**
     * Name of the current test suite
     */
    private String name;
    
    /**
     * @see junit.framework.TestSuite#TestSuite()
     */
    public AbstractTestSuite()
    {
    }

    /**
     * @see junit.framework.TestSuite#TestSuite(Class)
     */
    public AbstractTestSuite(final Class theClass)
    {
        setName(theClass.getName());
        Constructor constructor;
        try
        {
            // Avoid generating multiple error messages
            constructor = theClass.getConstructor(
                new Class[] { String.class });
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
     * @see junit.framework.TestSuite#TestSuite(String)
     */
    public AbstractTestSuite(String theName)
    {
        setName(theName);
    }

    /**
     * @see junit.framework.TestSuite#addTest(Test)
     */
    public void addTest(Test theTest)
    {
        this.tests.addElement(theTest);
    }

    /**
     * @see junit.framework.TestSuite#addTestSuite(Class)
     */
    public void addTestSuite(Class theTestClass)
    {
        addTest(createTestSuite(theTestClass));
    }

    /**
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

            Object[] args = new Object[] { name };
            try
            {
                // Note: We wrap the Test in a Cactus Test Case
                addTest(new ServletTestCase(name, 
                    (Test) theConstructor.newInstance(args)));
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
     * @see junit.framework.TestSuite#isPublicTestMethod(Method)
     */
    private boolean isPublicTestMethod(Method theMethod)
    {
        return isTestMethod(theMethod) 
            && Modifier.isPublic(theMethod.getModifiers());
    }

    /**
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
     * @see junit.framework.TestSuite#runTest(Test, TestResult)
     */
    public void runTest(Test theTest, TestResult theResult)
    {
        theTest.run(theResult);
    }
    
    /**
     * @see junit.framework.TestSuite#testAt(int)
     */
    public Test testAt(int theIndex)
    {
        return (Test) this.tests.elementAt(theIndex);
    }

    /**
     * @see junit.framework.TestSuite#testCount()
     */
    public int testCount()
    {
        return this.tests.size();
    }

    /**
     * @see junit.framework.TestSuite#tests()
     */
    public Enumeration tests()
    {
        return this.tests.elements();
    }

    /**
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
     * @see junit.framework.TestSuite#setName(String)
     */
    public void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * @see junit.framework.TestSuite#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
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
