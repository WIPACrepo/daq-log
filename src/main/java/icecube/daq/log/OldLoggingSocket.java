package icecube.daq.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

class OldLoggingSocket
    extends LoggingSocket
{
    private ByteArrayOutputStream byteOut;
    private PrintStream bytePrint;

    OldLoggingSocket(String hostname, int port)
        throws UnknownHostException, SocketException
    {
        super(hostname, port);
    }

    void formatAndSend(String loggerName, String threadName, String level,
                       Calendar date, String message, Throwable throwable)
    {
        StringBuilder msgBuf = new StringBuilder();

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
            String dateStr = String.format("%tF %tT.%tL", date, date, date);

            msgBuf.append(" [").append(dateStr).append("]");
        }

        if (message != null) {
            msgBuf.append(' ').append(message);
        }

        if (throwable != null) {
            if (byteOut == null) {
                byteOut = new ByteArrayOutputStream();
                bytePrint = new PrintStream(byteOut);
            }

            synchronized (byteOut) {
                byteOut.reset();
                throwable.printStackTrace(bytePrint);
                msgBuf.append('\n').append(byteOut.toString());
            }
        }

        sendMsg(msgBuf.toString());
    }
}
