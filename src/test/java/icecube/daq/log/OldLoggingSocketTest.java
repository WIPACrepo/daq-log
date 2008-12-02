package icecube.daq.log;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OldLoggingSocketTest
    extends TestCase
{
    private LogReader logRdr;
    private LoggingSocket logSock;

    public static Test suite()
    {
        return new TestSuite(OldLoggingSocketTest.class);
    }

    protected void setUp()
    {
        try {
            logRdr = new LogReader("log");
        } catch (IOException ioe) {
            fail("Couldn't create log reader: " + ioe.getMessage());
        }

        logSock = null;
    }

    protected void tearDown()
    {
        if (logSock != null) {
            logSock.close();
        }

        if (logRdr != null) {
            logRdr.close();

            if (logRdr.hasError()) fail(logRdr.getNextError());
            assertEquals("Not all log messages were received",
                         0, logRdr.getNumberOfExpectedMessages());
        }
    }

    private void waitForLogMessages()
    {
        for (int i = 0;
             !logRdr.hasError() && !logRdr.isFinished() && i < 10;
             i++)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore interrupts
            }
        }
        if (logRdr.hasError()) fail(logRdr.getNextError());
        assertEquals("Not all log messages were received",
                     0, logRdr.getNumberOfExpectedMessages());
    }

    public void testConnected()
        throws UnknownHostException, SocketException
    {
        String host = "localhost";
        String ipAddr = "127.0.0.1";
        int port = logRdr.getPort();

        logSock = new OldLoggingSocket(host, port);
        assertTrue("isConnected() should have matched " + host + ":" + port,
                   logSock.isConnected(host, port));
        assertTrue("isConnected() should have matched " + ipAddr + ":" + port,
                   logSock.isConnected(ipAddr, port));
    }

    public void testWrite()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        String name = "foo";
        String thrName = "thread";
        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";
        Exception thr;
        try {
            throw new Exception("Sample");
        } catch (Exception ex) {
            thr = ex;
        }

        String dateStr = String.format("%tF %tT.%tL", date, date, date);
        logRdr.addExpected(name + "-" + thrName + " " + level + " [" +
                           dateStr + "] " + message + "\n");

        logSock.write(name, thrName, level, date, message, thr);

        waitForLogMessages();
    }

    public void testWriteNoLogName()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        String thrName = "thread";
        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";
        Exception thr;
        try {
            throw new Exception("Sample");
        } catch (Exception ex) {
            thr = ex;
        }

        String dateStr = String.format("%tF %tT.%tL", date, date, date);
        logRdr.addExpected("???-" + thrName + " " + level + " [" +
                           dateStr + "] " + message + "\n");

        logSock.write(null, thrName, level, date, message, thr);

        waitForLogMessages();
    }

    public void testWriteNoThreadName()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        String name = "foo";
        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";
        Exception thr;
        try {
            throw new Exception("Sample");
        } catch (Exception ex) {
            thr = ex;
        }

        String dateStr = String.format("%tF %tT.%tL", date, date, date);
        logRdr.addExpected(name + " " + level + " [" + dateStr + "] " +
                           message + "\n");

        logSock.write(name, null, level, date, message, thr);

        waitForLogMessages();
    }

    public void testWriteNoNames()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";
        Exception thr;
        try {
            throw new Exception("Sample");
        } catch (Exception ex) {
            thr = ex;
        }

        String dateStr = String.format("%tF %tT.%tL", date, date, date);
        logRdr.addExpected("??? " + level + " [" + dateStr + "] " +
                           message + "\n");

        logSock.write(null, null, level, date, message, thr);

        waitForLogMessages();
    }

    public void testWriteNoNamesOrLevel()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        Calendar date = Calendar.getInstance();
        String message = "message";
        Exception thr;
        try {
            throw new Exception("Sample");
        } catch (Exception ex) {
            thr = ex;
        }

        String dateStr = String.format("%tF %tT.%tL", date, date, date);
        logRdr.addExpected("??? [" + dateStr + "] " + message + "\n");

        logSock.write(null, null, null, date, message, thr);

        waitForLogMessages();
    }

    public void testWriteNoNamesLevelDate()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        String message = "message";
        Exception thr;
        try {
            throw new Exception("Sample");
        } catch (Exception ex) {
            thr = ex;
        }

        logRdr.addExpected("??? " + message + "\n");

        logSock.write(null, null, null, null, message, thr);

        waitForLogMessages();
    }

    public void testWriteNothing()
        throws UnknownHostException, SocketException
    {
        logSock = new OldLoggingSocket("localhost", logRdr.getPort());

        logRdr.addExpected("???");

        logSock.write(null, null, null, null, null, null);

        waitForLogMessages();
    }

    /**
     * Main routine which runs text test in standalone mode.
     *
     * @param args the arguments with which to execute this method.
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}
