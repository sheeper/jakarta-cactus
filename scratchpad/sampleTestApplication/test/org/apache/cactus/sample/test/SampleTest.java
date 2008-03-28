package org.apache.cactus.sample.test;

import org.apache.cactus.sample.SampleServlet;
import org.testng.Assert;
/**
 * <code>Introduction50</code> is an introductory example to
 * illustrates the most basic features of TestNG using JDK5+ type
 * annotations.
 */
public class SampleTest
{
    /**
     * A simple test method.
     */
    //@Test
    public void theTestMethod50()
    {
    	SampleServlet sampleServlet = new SampleServlet();
        Assert.assertEquals(sampleServlet.doGet(), "Hello World");
    }
}