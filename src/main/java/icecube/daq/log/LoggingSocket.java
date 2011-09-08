package icecube.daq.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

abstract class LoggingSocket
{
    private InetAddress address;
    private int port;
    private DatagramSocket socket;

    LoggingSocket(String hostname, int port)
        throws UnknownHostException, SocketException
    {
        super();

        address = InetAddress.getByName(hostname);

        this.port = port;

        reconnect();
    }

    abstract void formatAndSend(String loggerName, String threadName,
                                String level, Calendar date,
                                String message, Throwable throwable);

    void close()
    {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    /**
     * Is this socket connected to a remote socket?
     *
     * @return <tt>true</tt> if this socket is connected to a remote socket
     */
    boolean isConnected()
    {
        return socket != null;
    }

    /**
     * Is this socket connected to the specified socket?
     *
     * @param hostname remote host name/address
     * @param port port number
     *
     * @return <tt>true</tt> if this socket is connected to the specified socket
     */
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

        return port == socket.getPort();
    }

    /**
     * Reconnect to logging socket.
     *
     * @throws SocketException if the connection could not be made
     */
    void reconnect()
        throws SocketException
    {
        if (socket != null) {
            throw new Error("LoggingSocket is already connected");
        }

        socket = new DatagramSocket();
        socket.connect(address, port);
    }

    /**
     * Send the message in a single datagram.
     *
     * @param msg message string
     */
    void sendMsg(String msg)
    {
        if (socket == null) {
            return;
        }

        byte[] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // silently drop the log message
        }
    }

    /** Try to write msg to sock; do nothing if fails */
    void write(String loggerName, String threadName, String level,
               Calendar date, String message, Throwable throwable)
    {
        if (socket == null) {
            return;
        }

        formatAndSend(loggerName, threadName, level, date, message,
                      throwable);
    }

    public String toString()
    {
        if (socket == null) {
            return "LoggingSocket[NULL]";
        }

        return "LoggingSocket[" + socket.getInetAddress() + "@" +
            socket.getPort() + "]";
    }
}
