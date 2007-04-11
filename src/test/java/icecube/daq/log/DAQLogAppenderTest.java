package icecube.daq.log;

import icecube.daq.log.DAQLogAppender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

class LogReader
{
    private DatagramSocket sock;
    private int port;
    private ArrayList<String> expList;
    private ArrayList<String> errorList;

    private boolean running;

    LogReader()
        throws IOException
    {
        sock = new DatagramSocket();
        port = sock.getLocalPort();

        expList = new ArrayList();
        errorList = new ArrayList();

        Thread thread = new Thread(new ReaderThread());
        thread.setName("ReaderThread");
        thread.start();
    }

    void addExpected(String msg)
    {
        expList.add(msg);
    }

    void close()
    {
        running = false;
    }

    String getNextError()
    {
        if (errorList.isEmpty()) {
            return null;
        }

        return errorList.remove(0);
    }

    int getPort()
    {
        return port;
    }

    boolean hasError()
    {
        return !errorList.isEmpty();
    }

    boolean isFinished()
    {
        return expList.isEmpty();
    }

    class ReaderThread
        implements Runnable
    {
        ReaderThread()
        {
        }

        public void run()
        {
            running = true;

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (running) {
                try {
                    sock.receive(packet);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    continue;
                }

                String fullMsg = new String(buf, 0, packet.getLength());

                String errMsg = null;
                if (expList.isEmpty()) {
                    errMsg = "Got unexpected log message: " + fullMsg;
                } else {
                    String expMsg = expList.remove(0);
                    if (!fullMsg.endsWith(expMsg)) {
                        errMsg = "Expected \"" + expMsg + "\", got \"" +
                            fullMsg + "\"";
                    }
                }

                if (errMsg != null) {
                    errorList.add(errMsg);
                }
            }

            sock.close();
        }
    }
}

public class DAQLogAppenderTest
    extends TestCase
{
    private static final Log LOG = LogFactory.getLog(DAQLogAppenderTest.class);

    private static final String LOGHOST = "localhost";
    private static final int LOGPORT = 6666;

    private LogReader logRdr;
    private DAQLogAppender appender;

    private void sendMsg(Level level, String msg)
    {
        if (level.isGreaterOrEqual(appender.getLevel())) {
            logRdr.addExpected(msg);
        }

        if (level.equals(Level.DEBUG)) {
            LOG.debug(msg);
        } else if (level.equals(Level.INFO)) {
            LOG.info(msg);
        } else if (level.equals(Level.WARN)) {
            LOG.warn(msg);
        } else if (level.equals(Level.ERROR)) {
            LOG.error(msg);
        } else if (level.equals(Level.FATAL)) {
            LOG.fatal(msg);
        } else {
            fail("Unknown log level " + level);
        }
    }

    protected void setUp()
    {
        try {
            logRdr = new LogReader();
        } catch (IOException ioe) {
            System.err.println("Couldn't create log reader");
            ioe.printStackTrace();
            logRdr = null;
        }

        try {
            appender = new DAQLogAppender(Level.INFO, "localhost",
                                          logRdr.getPort());
        } catch (Exception ex) {
            System.err.println("Couldn't create appender");
            ex.printStackTrace();
            appender = null;
        }

	BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure(appender);
    }

    public static Test suite()
    {
        return new TestSuite(DAQLogAppenderTest.class);
    }

    protected void tearDown()
    {
        appender.close();
        logRdr.close();

        if (logRdr.hasError()) {
            fail("LogReader had errors; first error is: " +
                 logRdr.getNextError());
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
        if (logRdr.hasError()) {
            fail(logRdr.getNextError());
        }
        if (!logRdr.isFinished()) {
            fail("Didn't see all log messages");
        }
    }

    public void testLog()
    {
	sendMsg(Level.INFO, "This is a test of logging.");
	sendMsg(Level.INFO, "This is test 2 of logging.");
	sendMsg(Level.WARN, "This is a WARN test.");
	sendMsg(Level.WARN, "This is a ERROR test.");
	sendMsg(Level.WARN, "This is a FATAL test.");
	sendMsg(Level.DEBUG, "This is a DEBUG test.");

        waitForLogMessages();

        for (int i = 0; i < 3; i++) {
	    sendMsg(Level.INFO, "This is test " + i + " of logging.");
            waitForLogMessages();
	}
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
