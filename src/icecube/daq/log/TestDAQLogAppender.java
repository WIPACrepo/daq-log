
package icecube.daq.log;

import java.net.UnknownHostException;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.log.DAQLogAppender;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

class TestDAQLogAppender {
    public static final void main(String[] args) throws Exception {
	String hostname = "localhost";
	int portnum     = 6666;

	if(args.length > 0) {
	    portnum = Integer.parseInt(args[0]);
	}
	
	System.out.println("writing to localhost port " + portnum);

	Log log = LogFactory.getLog(TestDAQLogAppender.class);

	BasicConfigurator.resetConfiguration();
	try {
	    BasicConfigurator.configure(new DAQLogAppender(Level.INFO,
							   hostname, 
							   portnum));
	} catch(UnknownHostException e) {
	    System.out.println(e);
	    return;
	}
	log.info("This is a test of logging.");
	log.info("This is test 2 of logging.");
	int i=3;
	while(true) {
	    log.info("This is test " + i + " of logging.");
	    Thread.sleep(1000);
	    i++;
	}
    }
}
