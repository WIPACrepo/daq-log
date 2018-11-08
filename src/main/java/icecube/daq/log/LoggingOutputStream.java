package icecube.daq.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * An OutputStream that writes contents to a Log upon each call to flush()
 *
 * From http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
 */
public class LoggingOutputStream
    extends ByteArrayOutputStream
{
    /** Original standard error stream */
    private static final PrintStream STDERR = System.err;

    private Logger logger;
    private Level level;

    private String lineSeparator;

    /**
     * Constructor
     * @param logger Logger to write to
     * @param level Level at which to write the log message
     */
    public LoggingOutputStream(Logger logger, Level level)
    {
        super();

        this.logger = logger;
        this.level = level;

        lineSeparator = System.getProperty("line.separator");
    }

    /**
     * upon flush() write the existing contents of the OutputStream
     * to the logger as a log record.
     * @throws java.io.IOException in case of error
     */
    public void flush()
        throws IOException
    {
        synchronized (this) {
            super.flush();

            String record = this.toString();
            if (record.endsWith(lineSeparator)) {
                record = record.substring(0, record.length() -
                                          lineSeparator.length());
            }

            super.reset();

            if (record.length() > 0) {
                if (isLooping()) {
                    STDERR.println(record);
                    STDERR.println("WARNING!  LoggingOutputStream is looping");
                }
                logger.log(level, record);
            }
        }
    }

    private synchronized boolean isLooping()
    {
        return false;
    }

    public void write(byte[] b)
        throws IOException
    {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len)
    {
        super.write(b, off, len);

        boolean needFlush = false;
        for (int i = off; i < len; i++) {
            if (b[i] == '\n') {
                needFlush = true;
                break;
            }
        }

        if (needFlush) {
            try {
                flush();
            } catch (IOException ioe) {
                throw new Error("Cannot write", ioe);
            }
        }
    }
}
