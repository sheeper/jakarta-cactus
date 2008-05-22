package org.apache.cactus.sample.ejb;

import java.util.*;
import javax.ejb.*;
import javax.jms.*;

public class EmailMDB implements MessageDrivenBean, MessageListener 
{

	private MessageDrivenContext context;

	// --------------------------------------------------------------
	// EJB Methods from MessageDrivenBean interface
	// --------------------------------------------------------------
	public void ejbCreate() {
		System.out.println("EmailMDB: ejbCreate called");
	}

	public void ejbRemove() {
		System.out.println("EmailMDB: ejbRemove called");
	}

	public void setMessageDrivenContext(MessageDrivenContext context) {
		System.out.println("setMessageDrivenContext called");
		this.context = context;
	}


	// --------------------------------------------------------------
	// Method from MessageListener interface
	// --------------------------------------------------------------
	/**
	 * Take in a MapMessage and use the EmailHelper to send out an email message
	 */
	public void onMessage(Message message) {
		System.out.println("EmailMDB: onMessage called");
		MapMessage mapmessage = (MapMessage) message;
		try {

      		// Go through the map message and create a map for sendmail(Map)
      		Enumeration e = mapmessage.getMapNames();
			Hashtable mail = new Hashtable();

        	while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String val = mapmessage.getString(key);
				mail.put(key, val);
			}

			System.out.println("Sending email: EmailHelper.sendmail(mail)");

        } catch(Exception e) {
           	   e.printStackTrace();
        }
	}
}
