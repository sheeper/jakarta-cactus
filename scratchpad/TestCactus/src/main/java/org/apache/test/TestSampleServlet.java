package org.apache.test;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.servlet.SampleServlet;

public class TestSampleServlet extends ServletTestCase {
	
	public void testGetServletName()
	{
	    SampleServlet servlet = new SampleServlet();
	    assertEquals("SampleServlet", servlet.getServletName());
	}
	
}
