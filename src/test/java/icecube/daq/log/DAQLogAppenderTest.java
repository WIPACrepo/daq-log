package icecube.daq.log;

import icecube.daq.log.DAQLogAppender;
import icecube.daq.log.LoggingOutputStream;

import java.io.IOException;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

public class DAQLogAppenderTest
    extends TestCase
{
    private static final Log LOG = LogFactory.getLog(DAQLogAppenderTest.class);

    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

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
        LogFactory.getFactory().releaseAll();

        System.setProperty("org.apache.commons.logging.Log",
                           "org.apache.commons.logging.impl.Log4JLogger");

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
        if (!STDOUT.equals(System.out)) {
            System.setOut(STDOUT);
        }
        if (!STDERR.equals(System.err)) {
            System.setErr(STDERR);
        }

        appender.close();
        logRdr.close();

        assertFalse(logRdr.getNextError(), logRdr.hasError());
        assertEquals("Not all log messages were received",
                     0, logRdr.getNumberOfExpectedMessages());
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
        assertFalse(logRdr.getNextError(), logRdr.hasError());
        assertEquals("Not all log messages were received",
                     0, logRdr.getNumberOfExpectedMessages());
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

    public void testRedirect()
    {
        Log errLog = LogFactory.getLog("STDERR");
        LoggingOutputStream logStream =
            new LoggingOutputStream(errLog, Level.ERROR);
        System.setErr(new PrintStream(logStream));

        String errMsg = "Error";
        logRdr.addExpected(errMsg);
        System.err.println(errMsg);
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
