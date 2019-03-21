package icecube.daq.log;

import icecube.daq.common.IDAQAppender;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Basic log4j appender.
 */
public class BasicAppender
    implements IDAQAppender
{
    /** minimum level of log messages which will be print. */
    private Level minLevel;

    /**
     * Create a BasicAppender which ignores everything below the WARN level.
     */
    public BasicAppender()
    {
        this(Level.WARN);
    }

    /**
     * Create a BasicAppender which ignores everything
     * below the specified level.
     *
     * @param minLevel minimum level
     */
    public BasicAppender(Level minLevel)
    {
        this.minLevel = minLevel;
    }

    /**
     * Unimplemented.
     *
     * @param filter ???
     */
    public void addFilter(Filter filter)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     */
    @Override
    public void clearFilters()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Nothing needs to be done here.
     */
    @Override
    public void close()
    {
        // don't need to do anything
    }

    /**
     * Handle a logging event.
     *
     * @param evt logging event
     */
    public void doAppend(LoggingEvent evt)
    {
        if (evt.getLevel().toInt() >= minLevel.toInt()) {
            LocationInfo loc = evt.getLocationInformation();

            System.out.println(evt.getLoggerName() + " " + evt.getLevel() +
                               " [" + loc.fullInfo + "] " + evt.getMessage());

            String[] stack = evt.getThrowableStrRep();
            for (int i = 0; stack != null && i < stack.length; i++) {
                System.out.println("> " + stack[i]);
            }
        }
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public ErrorHandler getErrorHandler()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public Filter getFilter()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public Layout getLayout()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the logging level.
     *
     * @return lowest leel of messages which will be logged
     */
    @Override
    public Level getLevel()
    {
        return minLevel;
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public String getName()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Basic appender is always connected to System.out.
     *
     * @return <tt>true</tt>
     */
    @Override
    public boolean isConnected(String logHost, int logPort, String liveHost,
                               int livePort)
    {
        return true;
    }

    /**
     * Basic appender is always connected to System.out.
     *
     * @return <tt>true</tt>
     */
    @Override
    public boolean isConnected()
    {
        return true;
    }

    /**
     * No need to reconnect to System.out, so this method does nothing.
     */
    @Override
    public void reconnect()
    {
        // do nothing
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public boolean requiresLayout()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @param handler ???
     */
    public void setErrorHandler(ErrorHandler handler)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @param layout ???
     */
    public void setLayout(Layout layout)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @param name ???
     */
    @Override
    public void setName(String name)
    {
        throw new Error("Unimplemented");
    }
}
