package org.apache.cactus.sample.ejb.redirector;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */

public interface ITestBean extends EJBObject {

	//METHODS
	int sum(int a, int b) throws RemoteException;
	int multiply(int a, int b) throws RemoteException;

}