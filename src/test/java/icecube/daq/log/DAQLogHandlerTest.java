package icecube.daq.log;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class TinyAppender
    implements org.apache.log4j.Appender
{
    private org.apache.log4j.Level minLevel;

    TinyAppender()
    {
        this.minLevel = org.apache.log4j.Level.INFO;
    }

    public void addFilter(org.apache.log4j.spi.Filter filter) { }
    public void clearFilters() { }
    public void close() { }

    public void doAppend(org.apache.log4j.spi.LoggingEvent evt)
    {
        if (evt.getLevel().toInt() >= minLevel.toInt()) {
            org.apache.log4j.spi.LocationInfo loc =
                evt.getLocationInformation();

            System.out.println("LOG4J: " + evt.getLoggerName() + " " +
                               evt.getLevel() + " [" + loc.fullInfo + "] " +
                               evt.getMessage());

            String[] stack = evt.getThrowableStrRep();
            for (int i = 0; stack != null && i < stack.length; i++) {
                System.out.println("> " + stack[i]);
            }
        }
    }

    public org.apache.log4j.spi.ErrorHandler getErrorHandler() { return null; }
    public org.apache.log4j.spi.Filter getFilter() { return null; }
    public org.apache.log4j.Layout getLayout() { return null; }
    public String getName() { return null; }
    public boolean requiresLayout() { return false; }
    public void setErrorHandler(org.apache.log4j.spi.ErrorHandler eh) { }
    public void setLayout(org.apache.log4j.Layout layout) { }
    public void setName(String name) { }
}

public class DAQLogHandlerTest
    extends TestCase
{
    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

    private static final String LOGHOST = "localhost";
    private static final int LOGPORT = 6666;

    private LogReader logRdr;
    private DAQLogHandler handler;

    private void sendMsg(Level level, String msg)
    {
        if (level.intValue() >= handler.getLevel().intValue()) {
            logRdr.addExpected(msg);
        }

        Log log = LogFactory.getLog(DAQLogHandlerTest.class);
        if (level.intValue() < Level.FINE.intValue()) {
            log.trace(msg);
        } else if (level.intValue() < Level.INFO.intValue()) {
            log.debug(msg);
        } else if (level.equals(Level.INFO)) {
            log.info(msg);
        } else if (level.equals(Level.WARNING)) {
            log.warn(msg);
        } else if (level.equals(Level.SEVERE)) {
            log.fatal(msg);
        } else {
            fail("Unknown log level " + level);
        }
    }

    @Override
    protected void setUp()
    {
        LogFactory.getFactory().releaseAll();

        System.setProperty("org.apache.commons.logging.Log",
                           "org.apache.commons.logging.impl.Jdk14Logger");

        org.apache.log4j.BasicConfigurator.resetConfiguration();
        org.apache.log4j.BasicConfigurator.configure(new TinyAppender());

        try {
            logRdr = new LogReader("log");
        } catch (IOException ioe) {
            System.err.println("Couldn't create log reader");
            ioe.printStackTrace();
            logRdr = null;
        }

        try {
            handler = new DAQLogHandler("xxx", Level.INFO, "localhost",
                                        logRdr.getPort(), null, 0);
        } catch (Exception ex) {
            System.err.println("Couldn't create handler");
            ex.printStackTrace();
            handler = null;
        }

        // find base logger
        Logger baseLogger = Logger.getLogger("");
        while (baseLogger.getParent() != null) {
            baseLogger = baseLogger.getParent();
        }

        // clear out default handlers
        Handler[] hList = baseLogger.getHandlers();
        for (int i = 0; i < hList.length; i++) {
            baseLogger.removeHandler(hList[i]);
        }

        baseLogger.addHandler(handler);
    }

    public static Test suite()
    {
        return new TestSuite(DAQLogHandlerTest.class);
    }

    @Override
    protected void tearDown()
    {
        if (!STDOUT.equals(System.out)) {
            System.setOut(STDOUT);
        }
        if (!STDERR.equals(System.err)) {
            System.setErr(STDERR);
        }

        handler.close();
        logRdr.close();

        if (logRdr.hasError()) fail(logRdr.getNextError());
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
        if (logRdr.hasError()) fail(logRdr.getNextError());
        assertEquals("Not all log messages were received",
                     0, logRdr.getNumberOfExpectedMessages());
    }

    public void testLog()
    {
        sendMsg(Level.FINEST, "This is a FINEST test.");
        sendMsg(Level.INFO, "This is a test of logging.");
        sendMsg(Level.INFO, "This is test 2 of logging.");
        sendMsg(Level.WARNING, "This is a WARNING test.");
        sendMsg(Level.SEVERE, "This is a SEVERE test.");
        sendMsg(Level.CONFIG, "This is a CONFIG test.");
        sendMsg(Level.FINE, "This is a FINE test.");
        sendMsg(Level.FINER, "This is a FINER test.");

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
            new LoggingOutputStream(errLog, org.apache.log4j.Level.ERROR);
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
