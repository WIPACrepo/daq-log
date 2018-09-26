package icecube.daq.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;

class LogReader
{
    private String name;
    private DatagramSocket sock;
    private int port;
    private ArrayList<String> expList;
    private ArrayList<String> errorList;

    private boolean running;

    LogReader(String name)
        throws IOException
    {
        this.name = name;

        sock = new DatagramSocket();
        port = sock.getLocalPort();

        expList = new ArrayList();
        errorList = new ArrayList();

        Thread thread = new Thread(new ReaderThread());
        thread.setName("ReaderThread-" + name);
        thread.start();
    }

    void addExpected(String msg)
    {
        synchronized (expList) {
            expList.add(msg);
        }
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

        @Override
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

                int lastChar = fullMsg.length() - 1;
                if (lastChar >= 0 && fullMsg.charAt(lastChar) == '\n') {
                    fullMsg = fullMsg.substring(0, lastChar);
                }

                String errMsg = null;
                if (fullMsg == null) {
                    errMsg = "fullMsg is null";
                } else synchronized (expList) {
                    if (expList.isEmpty()) {
                        errMsg = "Got unexpected " + name + " message: " +
                            fullMsg;
                    } else {
                        String expMsg = expList.remove(0);
                        if (expMsg == null) {
                            errMsg = "expMsg is null";
                        } else if (!fullMsg.startsWith(expMsg) &&
                                   !fullMsg.endsWith(expMsg))
                        {
                            errMsg = "Expected " + name + " \"" + expMsg +
                                "\", got \"" + fullMsg + "\"";
                        }
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
