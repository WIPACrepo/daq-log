package icecube.daq.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class LoggingSocket
{
    private DatagramSocket socket;
    private StringBuffer msgBuf;
    private ByteArrayOutputStream byteOut;
    private PrintStream bytePrint;

    LoggingSocket(String hostname, int port)
        throws UnknownHostException, SocketException
    {
        super();

        InetAddress address = InetAddress.getByName(hostname);

        this.socket = new DatagramSocket();
        socket.connect(address, port);
    }

    private String buildMessage(String loggerName, String threadName,
                                String level, String date, String message,
                                Throwable throwable)
    {
        if (msgBuf == null) {
            msgBuf = new StringBuffer();
        } else {
            msgBuf.setLength(0);
        }

        if (loggerName != null) {
            msgBuf.append(loggerName);
            if (threadName != null) {
                msgBuf.append('-').append(threadName);
            }
        } else if (threadName != null) {
            msgBuf.append("???-").append(threadName);
        } else {
            msgBuf.append("???");
        }

        if (level != null) {
            msgBuf.append(' ').append(level);
        }

        if (date != null) {
            msgBuf.append(" [").append(date).append("] ");
        }

        msgBuf.append(message);

        if (throwable != null) {
            if (byteOut == null) {
                byteOut = new ByteArrayOutputStream();
                bytePrint = new PrintStream(byteOut);
            } else {
                byteOut.reset();
            }

            throwable.printStackTrace(bytePrint);
            msgBuf.append('\n').append(byteOut.toString());
        }

        return msgBuf.toString();
    }

    void close()
    {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    boolean isConnected(String hostname, int port)
    {
        if (socket == null) {
            return false;
        }

        InetAddress addr;
        try {
            addr = InetAddress.getByName(hostname);
        } catch (UnknownHostException uhe) {
            return false;
        }

        if (addr == null || !addr.equals(socket.getInetAddress())) {
            return false;
        }

        return port == socket.getLocalPort();
    }

    /** Try to write msg to sock; do nothing if fails */
    void write(String loggerName, String threadName, String level, String date,
               String message, Throwable throwable)
    {
        if (socket == null) {
            return;
        }

        String msg = buildMessage(loggerName, threadName, level, date, message,
                                  throwable);

        byte[] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.send(packet);
        } catch(IOException e) {
            // silently drop the log message
        }
    }

}
