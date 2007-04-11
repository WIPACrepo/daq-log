package icecube.daq.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;

import org.apache.log4j.Level;

/**
 * An OutputStream that writes contents to a Log upon each call to flush()
 *
 * From http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
 */
class LoggingOutputStream
    extends ByteArrayOutputStream
{
    private String lineSeparator;

    private Log logger;
    private Level level;

    /**
     * Constructor
     * @param logger Logger to write to
     * @param level Level at which to write the log message
     */
    public LoggingOutputStream(Log logger, Level level)
    {
        super();

        this.logger = logger;
        this.level = level;

        lineSeparator = System.getProperty("line.separator");
    }

    public void write(byte[] b)
        throws IOException
    {
        super.write(b);
            flush();
    }

    public void write(byte[] b, int off, int len)
    {
        super.write(b, off, len);
        try {
            flush();
        } catch (IOException ioe) {
            throw new Error("Cannot write", ioe);
        }
    }

    /**
     * upon flush() write the existing contents of the OutputStream
     * to the logger as a log record.
     * @throws java.io.IOException in case of error
     */
    public void flush()
        throws IOException
    {
        synchronized(this) {
            super.flush();

            String record = this.toString();
            if (record.endsWith(lineSeparator)) {
                record = record.substring(0, record.length() -
                                          lineSeparator.length());
            }

            super.reset();

            if (record.length() > 0) {
                if (level.isGreaterOrEqual(Level.FATAL)) {
                    logger.fatal(record);
                } else if (level.isGreaterOrEqual(Level.ERROR)) {
                    logger.error(record);
                } else if (level.isGreaterOrEqual(Level.WARN)) {
                    logger.warn(record);
                } else if (level.isGreaterOrEqual(Level.INFO)) {
                    logger.info(record);
                } else {
                    logger.debug(record);
                }
            }
        }
    }
}
