package org.apache.cactus.sample.ejb.redirector;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */

public class TestBean implements SessionBean {

	//Attributes
	private SessionContext context;

	//METHODS

	public int sum(int a, int b) {
		return a + b;
	}
	public int multiply(int a, int b) {
		return a * b;
	}

	/**
	 * No argument constructor needed by the Container
	 */
	public TestBean() {

	}
	/**
	 * Create method specified in EJB 1.1 section 6.10.3
	 */
	public void ejbCreate() throws CreateException {
	}

	/* Methods required by SessionBean Interface. EJB 1.1 section 6.5.1. */

	/**
	 * @see javax.ejb.SessionBean#setContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext context) {
		this.context = context;
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() {
	}

}
