package org.apache.cactus.sample.ejb.redirector;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */

public interface TestHome extends EJBHome {
	public ITestBean create() throws RemoteException, CreateException;

}
