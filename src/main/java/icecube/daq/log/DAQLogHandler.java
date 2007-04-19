package icecube.daq.log;

import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.Date;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class DAQLogHandler
    extends Handler
{
    private LoggingSocket socket;

    public DAQLogHandler(Level minLevel, String hostname, int port)
        throws UnknownHostException, SocketException
    {
        super();

        setLevel(minLevel);

        socket = new LoggingSocket(hostname, port);
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord rec) {
        if (isLoggable(rec)) {
            String threadName = "Thread#" + rec.getThreadID();
            String dateStr = new Date(rec.getMillis()).toString();
            socket.write(rec.getLoggerName(), threadName,
                         rec.getLevel().toString(), dateStr,
                         rec.getMessage(), rec.getThrown());
        }
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#flush()
     */
    public void flush() {
        // nothing to flush
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#close()
     */
    public void close() throws SecurityException {
        socket.close();
    }
}
