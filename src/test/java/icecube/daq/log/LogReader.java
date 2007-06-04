package icecube.daq.log;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.ArrayList;
import java.util.Iterator;

class LogReader
{
    private DatagramSocket sock;
    private int port;
    private ArrayList<String> expList;
    private ArrayList<String> errorList;

    private boolean running;

    LogReader()
        throws IOException
    {
        sock = new DatagramSocket();
        port = sock.getLocalPort();

        expList = new ArrayList();
        errorList = new ArrayList();

        Thread thread = new Thread(new ReaderThread());
        thread.setName("ReaderThread");
        thread.start();
    }

    void addExpected(String msg)
    {
        expList.add(msg);
    }

    void close()
    {
        running = false;
    }

    String getNextError()
    {
        if (errorList.isEmpty()) {
            return null;
        }

        return errorList.remove(0);
    }

    int getNumberOfExpectedMessages()
    {
        return expList.size();
    }

    int getPort()
    {
        return port;
    }

    boolean hasError()
    {
        return !errorList.isEmpty();
    }

    boolean isFinished()
    {
        return expList.isEmpty();
    }

    class ReaderThread
        implements Runnable
    {
        ReaderThread()
        {
        }

        public void run()
        {
            running = true;

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (running) {
                try {
                    sock.receive(packet);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    continue;
                }

                String fullMsg = new String(buf, 0, packet.getLength());
                String errMsg = null;
                if (expList.isEmpty()) {
                    errMsg = "Got unexpected log message: " + fullMsg;
                } else {
                    String expMsg = expList.remove(0);
                    if (!fullMsg.endsWith(expMsg)) {
                        errMsg = "Expected \"" + expMsg + "\", got \"" +
                            fullMsg + "\"";
                    }
                }

                if (errMsg != null) {
                    errorList.add(errMsg);
                }
            }

            sock.close();
        }
    }
}
