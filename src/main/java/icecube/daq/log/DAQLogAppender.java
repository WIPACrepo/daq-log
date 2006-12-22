
package icecube.daq.log;

import java.io.*;
import java.net.*;
import java.util.Date;

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

	private String remoteHost;
	private int    remotePort;
	private InetAddress remoteAddress = null;
	private DatagramSocket socket = null;

	/** Minimum log level */
	private Level minLevel; 

	/** General DAQLogAppender constructor */
	public DAQLogAppender(Level minLevel, String hostname, int port) 
	throws UnknownHostException, SocketException
	{
		this.minLevel   = minLevel;
		this.remoteHost = hostname;
		this.remotePort = port;
		this.remoteAddress = InetAddress.getByName(this.remoteHost);
		this.socket = new DatagramSocket();
		socket.connect(this.remoteAddress, this.remotePort);
	}

	public boolean requiresLayout()      { return false; }

	public void close() { /* Close socket here eventually */ }

	/** Try to write msg to sock; do nothing if fails */
	private void writeEntry(DatagramSocket sock, String msg) {
		byte[] buf = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			socket.send(packet);
		} catch(IOException e) {
		}
	}

	/** Adapted from MockAppender */
	public void doAppend(LoggingEvent evt) {
		if (evt.getLevel().toInt() >= minLevel.toInt()) {
			String date = new Date().toString();
			String entry = evt.getLoggerName() + " " +
			evt.getLevel() + " [" + date +	
			"] " + evt.getMessage();

			writeEntry(socket, entry);

			String[] stack = evt.getThrowableStrRep();
			for (int i = 0; stack != null && i < stack.length; i++) {
				entry = "> " + stack[i];
				writeEntry(socket, entry);
			}
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

}


