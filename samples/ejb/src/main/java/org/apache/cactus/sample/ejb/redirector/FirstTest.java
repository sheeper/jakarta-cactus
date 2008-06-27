package org.apache.cactus.sample.ejb.redirector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.*;
/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */

public class FirstTest extends EJBTestCase {

	ITestBean test;

	public FirstTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FirstTest.class);
	}

	public void setUp() {
		System.out.println("Set Up for TestBean");
		try {
			Context ctx = new InitialContext();
			HomeTestBean home =
				(HomeTestBean) PortableRemoteObject.narrow(
					ctx.lookup("CACTUS/TEST"),
					HomeTestBean.class);
			test = home.create();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testSum() throws Exception {
		System.out.println("Test for SUM");
		assertEquals(3, test.sum(2, 2));
	}
	public void testMultiply() throws Exception {
		System.out.println("Test for Multiply");
		assertEquals(4, test.multiply(2, 2));
	}
	public void tearDown() {
		System.out.println("Tear Down for TestBean");
		test = null;
	}
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

}
