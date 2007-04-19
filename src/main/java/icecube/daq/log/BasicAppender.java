package icecube.daq.log;

import org.apache.log4j.Appender;
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
    implements Appender
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
    public void clearFilters()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Nothing needs to be done here.
     */
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
     * Unimplemented.
     *
     * @return ???
     */
    public String getName()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
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
    public void setName(String name)
    {
        throw new Error("Unimplemented");
    }
}
