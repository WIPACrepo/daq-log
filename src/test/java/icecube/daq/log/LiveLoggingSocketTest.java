package icecube.daq.log;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LiveLoggingSocketTest
    extends TestCase
{
    private LogReader logRdr;
    private LoggingSocket logSock;

    public static Test suite()
    {
        return new TestSuite(LiveLoggingSocketTest.class);
    }

    protected void setUp()
    {
        try {
            logRdr = new LogReader("live");
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
        String svc = "SeRvIcE";
        String host = "localhost";
        String ipAddr = "127.0.0.1";
        int port = logRdr.getPort();

        logSock = new LiveLoggingSocket(svc, host, port);
        assertTrue("isConnected() should have matched " + host + ":" + port,
                   logSock.isConnected(host, port));
        assertTrue("isConnected() should have matched " + ipAddr + ":" + port,
                   logSock.isConnected(ipAddr, port));
    }

    public void testWrite()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        String name = "foo";
        String thrName = "thread";
        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);
        logRdr.addExpected("pdaq(log:str) " + prio + " [" + dateStr + "] " +
                           level + " " + name + "-" + thrName + " " + message);

        logSock.write(name, thrName, level, date, message, null);

        waitForLogMessages();
    }

    public void testLongWrite()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        String name = "foo";
        String thrName = "thread";
        String level = "LVL";
        Calendar date = Calendar.getInstance();

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);

        StringBuilder msgBuf = new StringBuilder("xxxxx");
        for (int i = 0; i < LiveLoggingSocket.LIVE_MSGMAX; i++) {
            msgBuf.append('x');
        }

        String longMsg = msgBuf.toString();

        String front = "pdaq(log:str) " + prio + " [" + dateStr + "] " +
            level + " ";
        String middle = name + "-" + thrName + " ";

        final int maxLen = LiveLoggingSocket.LIVE_MSGMAX;
        while (msgBuf.length() > 0) {
            final int hdrLen = front.length() + middle.length();
            if (hdrLen + msgBuf.length() < maxLen) {
                logRdr.addExpected(front + middle + msgBuf.toString());
                msgBuf.setLength(0);
            } else {
                final int subLen = maxLen - hdrLen;
                logRdr.addExpected(front + middle +
                                   msgBuf.substring(0, subLen));
                msgBuf.delete(0, subLen);
            }

            middle = "";
        }

        logSock.write(name, thrName, level, date, longMsg, null);

        waitForLogMessages();
    }

    public void testWriteNoLogName()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        String name = "foo";
        String thrName = "thread";
        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);
        logRdr.addExpected("pdaq(log:str) " + prio + " [" + dateStr + "] " +
                           level + " ???-" + thrName + " " + message);

        logSock.write(null, thrName, level, date, message, null);

        waitForLogMessages();
    }

    public void testWriteNoThreadName()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        String name = "foo";
        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);
        logRdr.addExpected("pdaq(log:str) " + prio + " [" + dateStr + "] " +
                           level + " " + name + " " + message);

        logSock.write(name, null, level, date, message, null);

        waitForLogMessages();
    }

    public void testWriteNoNames()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        String level = "LVL";
        Calendar date = Calendar.getInstance();
        String message = "message";

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);
        logRdr.addExpected("pdaq(log:str) " + prio + " [" + dateStr + "] " +
                           level + " ??? " + message);

        logSock.write(null, null, level, date, message, null);

        waitForLogMessages();
    }

    public void testWriteNoNamesOrLevel()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        Calendar date = Calendar.getInstance();
        String message = "message";

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);
        logRdr.addExpected("pdaq(log:str) " + prio + " [" + dateStr +
                           "] ??? " + message);

        logSock.write(null, null, null, date, message, null);

        waitForLogMessages();
    }

    public void testWriteNothing()
        throws UnknownHostException, SocketException
    {
        final String svc = "Service";

        logSock = new LiveLoggingSocket(svc, "localhost", logRdr.getPort());

        Calendar date = Calendar.getInstance();

        final int prio = LiveLoggingSocket.LIVE_PRIORITY;
        final String dateStr =
            String.format("%tF %tT.%tL000", date, date, date);
        logRdr.addExpected("pdaq(log:str) " + prio + " [" + dateStr +
                           "] ???");

        logSock.write(null, null, null, date, null, null);

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
