package org.apache.cactus.eclipse.launcher;

import org.eclipse.jdt.internal.junit.runner.ITestRunListener;
import org.eclipse.jdt.internal.junit.ui.RemoteTestRunnerClient;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Launch shortcut used to start the Cactus launch configuration on the
 * current workbench selection.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id: $
 */
public class JUnitViewFinder implements Runnable
{
    /**
     * Reference to the current WorkBench page.
     */
    private IWorkbenchPage wbPage;
    /**
     * Listener that will be notified of test events.
     */
    private ITestRunListener listener;

    /**
     * Constructor
     * @param thePage the page to search
     * @param theListener the listener to notify 
     */
    public JUnitViewFinder(IWorkbenchPage thePage, ITestRunListener theListener)
    {
        wbPage = thePage;
        listener = theListener;
    }
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        IViewPart view = null;
        boolean foundView = false;
        while (!foundView)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                foundView = true;
            }
            view = wbPage.findView(TestRunnerViewPart.NAME);
            foundView = (view != null);
        }
        if (view != null)
        {
            TestRunnerViewPart jUnitView = (TestRunnerViewPart) view;
            boolean vmLaunchedAndRunning = false;
            RemoteTestRunnerClient client = null;
            while (!vmLaunchedAndRunning)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    vmLaunchedAndRunning = true;
                }
                client = jUnitView.getRemoteTestRunnerClient();
                vmLaunchedAndRunning = (client != null && client.isRunning());
            }
            // the tests might have ended already
            if (client != null && client.isRunning())
            {
                client.addListener(listener);
            }
            else
            {
                // notify the listener that the tests have
                // already ended
                listener.testRunEnded(0);
            }

        }
    }
}
