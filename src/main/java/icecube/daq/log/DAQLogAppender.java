package icecube.daq.log;

import icecube.daq.common.IDAQAppender;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Forward org.apache.log4j messages to DAQ logger.
 * @author John Jacobsen, NPX Designs, Inc. for UW-Madison IceCube.
 */
public class DAQLogAppender implements IDAQAppender
{
    private LiveLoggingSocket liveSocket;
    private OldLoggingSocket logSocket;

    /** Minimum log level */
    private Level minLevel;

    /**
     * Create a Log4J log appender.
     *
     * @param compName name of component/service
     * @param minLevel lowest level of messages to be forwarded
     * @param logHost DAQ logger host name
     * @param logPort DAQ logger network port number
     * @param liveHost I3Live logger host name
     * @param livePort I3Live logger network port number
     */
    public DAQLogAppender(String compName, Level minLevel, String logHost,
                          int logPort, String liveHost, int livePort)
        throws UnknownHostException, SocketException
    {
        this.minLevel   = minLevel;

        if (liveHost != null && livePort > 0) {
            liveSocket = new LiveLoggingSocket(compName, liveHost, livePort);
        }
        if (logHost != null && logPort > 0) {
            logSocket = new OldLoggingSocket(logHost, logPort);
        }

        if (liveSocket == null && logSocket == null) {
            throw new SocketException("No logging socket was created");
        }
    }

    @Override
    public boolean requiresLayout()
    {
        return false;
    }

    @Override
    public void close()
    {
        if (liveSocket != null) {
            liveSocket.close();
        }
        if (logSocket != null) {
            logSocket.close();
        }
    }

    /** Adapted from MockAppender */
    public void doAppend(LoggingEvent evt)
    {
        if (evt.getLevel().isGreaterOrEqual(minLevel)) {
            String level;
            if (evt.getLevel() == null) {
                level = "UNKNOWN";
            } else {
                level = evt.getLevel().toString();
            }

            String msg;
            if (evt.getMessage() == null) {
                msg = "";
            } else {
                msg = evt.getMessage().toString();
            }

            Throwable throwable;
            if (evt.getThrowableInformation() == null) {
                throwable = null;
            } else {
                throwable =
                    evt.getThrowableInformation().getThrowable();
            }

            Calendar now = Calendar.getInstance();
            now.setTime(new Date(evt.timeStamp));

            if (liveSocket != null) {
                liveSocket.write(evt.getLoggerName(), evt.getThreadName(),
                                 level, now, msg, throwable);
            }

            if (logSocket != null) {
                logSocket.write(evt.getLoggerName(), evt.getThreadName(),
                                level, now, msg, throwable);
            }
        }
    }

    public void addFilter(Filter newFilter)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public void clearFilters()
    {
        throw new Error("Unimplemented");
    }

    public ErrorHandler getErrorHandler()
    {
        throw new Error("Unimplemented");
    }

    public Filter getFilter()
    {
        throw new Error("Unimplemented");
    }

    public Layout getLayout()
    {
        throw new Error("Unimplemented");
    }


    @Override
    public String getName()
    {
        throw new Error("Unimplemented");
    }

    public void setLayout(Layout layout)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public void setName(String name)
    {
        throw new Error("Unimplemented");
    }

    public void setErrorHandler(ErrorHandler errorHandler)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public Level getLevel()
    {
        return minLevel;
    }

    @Override
    public boolean isConnected()
    {
        if (liveSocket != null && liveSocket.isConnected()) {
            return true;
        }
        if (logSocket != null && logSocket.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnected(String logHost, int logPort,
                               String liveHost, int livePort)
    {
        if (liveSocket != null && liveSocket.isConnected(liveHost, livePort)) {
            return true;
        }
        if (logSocket != null && logSocket.isConnected(logHost, logPort)) {
            return true;
        }
        return false;
    }

    @Override
    public void reconnect()
        throws SocketException
    {
        if (liveSocket != null) {
            liveSocket.reconnect();
        }
        if (logSocket != null) {
            logSocket.reconnect();
        }
    }

    @Override
    public String toString()
    {
        return "DAQLogAppender[" + liveSocket + "+" + logSocket + "@" +
            minLevel + "]";
    }
}
