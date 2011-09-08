package icecube.daq.log;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Forward java.util.logging messages to DAQ logger.
 */
public class DAQLogHandler
    extends Handler
{
    private LiveLoggingSocket liveSocket;
    private OldLoggingSocket logSocket;

    /**
     * Create a java.util.logging handler.
     *
     * @param compName name of component/service
     * @param minLevel lowest level of messages to be forwarded
     * @param logHost DAQ logger host name
     * @param logPort DAQ logger network port number
     * @param liveHost I3Live logger host name
     * @param livePort I3Live logger network port number
     */
    public DAQLogHandler(String compName, Level minLevel, String logHost,
                         int logPort, String liveHost, int livePort)
        throws UnknownHostException, SocketException
    {
        super();

        setLevel(minLevel);

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

    /* (non-API documentation)
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord rec) 
    {
        if (isLoggable(rec)) {
            String threadName = "Thread#" + rec.getThreadID();

            String level;
            if (rec.getLevel() == null) {
                level = "UNKNOWN";
            } else {
                level = rec.getLevel().toString();
            }

            String msg;
            if (rec.getMessage() == null) {
                msg = "";
            } else {
                msg = rec.getMessage().toString();
            }

            Calendar now = Calendar.getInstance();
            now.setTime(new Date(rec.getMillis()));

            if (liveSocket != null) {
                liveSocket.write(rec.getLoggerName(), threadName,
                                 level, now, msg, rec.getThrown());
            }

            if (logSocket != null) {
                logSocket.write(rec.getLoggerName(), threadName,
                                level, now, msg, rec.getThrown());
            }
        }
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#flush()
     */
    public void flush() 
    {
        // nothing to flush
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#close()
     */
    public void close() throws SecurityException 
    {
        if (liveSocket != null) {
            liveSocket.close();
        }
        if (logSocket != null) {
            logSocket.close();
        }
    }
}
