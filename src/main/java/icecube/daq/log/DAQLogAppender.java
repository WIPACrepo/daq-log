package icecube.daq.log;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Formatter;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;

import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender for IceCube pDAQ python UDP socket consumer
 * @author John Jacobsen, NPX Designs, Inc. for UW-Madison IceCube.
 */
public class DAQLogAppender implements Appender {

	private LoggingSocket socket;

	/** Minimum log level */
	private Level minLevel; 

	/** General DAQLogAppender constructor */
	public DAQLogAppender(Level minLevel, String hostname, int port) 
	throws UnknownHostException, SocketException
	{
		this.minLevel   = minLevel;

		socket = new LoggingSocket(hostname, port);
	}

	public boolean requiresLayout()      { return false; }

	public void close() { socket.close(); }

	/** Adapted from MockAppender */
	public void doAppend(LoggingEvent evt) {
		if (evt.getLevel().isGreaterOrEqual(minLevel)) {
			Throwable throwable;
			if (evt.getThrowableInformation() == null) {
				throwable = null;
			} else {
				throwable =
				   evt.getThrowableInformation().getThrowable();
			}
			Calendar now = Calendar.getInstance();
                        now.setTime(new Date(evt.timeStamp));
			socket.write(evt.getLoggerName(), evt.getThreadName(),
                                     evt.getLevel().toString(),
                                     String.format("%tF %tT.%tL", now, now, now),
                                     evt.getMessage().toString(), throwable);
		}
	}

	public void addFilter(Filter newFilter) { throw new Error("Unimplemented"); }
	public void clearFilters()              { throw new Error("Unimplemented"); }
	public ErrorHandler getErrorHandler()   { throw new Error("Unimplemented"); }
	public Filter getFilter()               { throw new Error("Unimplemented"); }
	public Layout getLayout()               { throw new Error("Unimplemented"); }
	public String getName()                 { throw new Error("Unimplemented"); }
	public void setLayout(Layout layout)    { throw new Error("Unimplemented"); }
	public void setName(String name)        { throw new Error("Unimplemented"); }
	public void setErrorHandler(ErrorHandler errorHandler) { 
		throw new Error("Unimplemented"); 
	}

	public Level getLevel()                 { return minLevel; }

    public boolean isConnected(String host, int port)
    {
        return socket.isConnected(host, port);
    }
}
