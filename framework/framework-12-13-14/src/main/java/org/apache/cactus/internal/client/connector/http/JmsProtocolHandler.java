package org.apache.cactus.internal.client.connector.http;

import javax.jms.QueueSession;

import junit.framework.Test;

import org.apache.cactus.JmsRequest;
import org.apache.cactus.Request;
import org.apache.cactus.ServiceDefinition;
import org.apache.cactus.internal.client.jms.JmsClient;
import org.apache.cactus.internal.client.jms.JmsClientHelper;
import org.apache.cactus.internal.util.JUnitVersionHelper;
import org.apache.cactus.spi.client.ResponseObjectFactory;
import org.apache.cactus.spi.client.connector.ProtocolHandler;
import org.apache.cactus.spi.client.connector.ProtocolState;
import org.apache.cactus.util.JmsConfiguration;

public class JmsProtocolHandler implements ProtocolHandler
{
    /**
     * Cactus configuration data to use. In particular contains useful 
     * configuration data for the HTTP connector (e.g. redirector URL).
     */
    private JmsConfiguration configuration;
    
    private QueueSession session;

    /**
     * @param theConfiguration configuration data
     */
    public JmsProtocolHandler(JmsConfiguration theConfiguration)
    {
        this.configuration = theConfiguration;
    }
    
	public void afterTest(ProtocolState theState) throws Exception 
	{
		// We simply do nothing here.		
	}

	public Request createRequest() {
		return new JmsRequest(session);
	}

	public ResponseObjectFactory createResponseObjectFactory(
			ProtocolState theState) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProtocolState runTest(Test theDelegatedTest, Test theWrappedTest,
			Request theRequest) throws Throwable 
	{
        // Create the JMS Request object and creates necessary JMS objects
        // so that the user can get them in his beginXXX method, so that he
        // can create the message to send.
        JmsRequest request = new JmsRequest(
            JmsClientHelper.getQueueSession());

        // Add Cactus information to the JMS Message
        request.getMessage().setStringProperty(
            ServiceDefinition.CLASS_NAME_PARAM, theDelegatedTest.getClass().getName());
        request.getMessage().setStringProperty(
            ServiceDefinition.METHOD_NAME_PARAM, (getCurrentTestName(theDelegatedTest)));

        // Start the test
        new JmsClient().doTest(request);
        
        return new JmsProtocolState();
	}
	
	
    /**
     * @return configuration data
     */
    private JmsConfiguration getConfiguration()
    {
        return this.configuration;
    }
    
    /**
     * @param theDelegatedTest the Cactus test to execute
     * @return the name of the current test case being executed (it corresponds
     *         to the name of the test method with the "test" prefix removed.
     *         For example, for "testSomeTestOk" would return "someTestOk".
     */
    private String getCurrentTestName(Test theDelegatedTest)
    {
        return JUnitVersionHelper.getTestCaseName(theDelegatedTest);        
    }

}