package icecube.daq.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

class LiveLoggingSocket
    extends LoggingSocket
{
    /** Hardcoded I3Live priority level for log messages */
    public static final int LIVE_PRIORITY = 4;

    /** Maximum I3Live message length */
    public static final int LIVE_MSGMAX = 222;

    private String service;

    private ByteArrayOutputStream byteOut;
    private PrintStream bytePrint;

    LiveLoggingSocket(String service, String hostname, int port)
        throws UnknownHostException, SocketException
    {
        super(hostname, port);

        //this.service = service;
        //I3Live expects service to be 'pdaq'
        this.service = "pdaq";
    }

    void formatAndSend(String loggerName, String threadName, String level,
                       Calendar date, String message, Throwable throwable)
    {
        if (date == null) {
            date = Calendar.getInstance();
        }

        String header =
            String.format("%s(log:str) %d [%tF %tT.%tL000] ", service,
                          LIVE_PRIORITY, date, date, date);

        if (level != null) {
            header += level + " ";
        }

        String middle;
        if (loggerName != null) {
            if (threadName == null) {
                middle = loggerName;
            } else {
                middle = loggerName + "-" + threadName;
            }
        } else if (threadName != null) {
            middle = "???-" + threadName;
        } else {
            middle = "???";
        }

        String content;
        if (throwable == null) {
            content = message;
        } else {
            StringBuilder msgBuf;
            if (message != null) {
                msgBuf = new StringBuilder(message);
            } else {
                msgBuf = new StringBuilder();
            }

            if (byteOut == null) {
                byteOut = new ByteArrayOutputStream();
                bytePrint = new PrintStream(byteOut);
            } else {
                byteOut.reset();
            }

            throwable.printStackTrace(bytePrint);

            /*
             * I3Live only allows single-line log msgs, so join stack trace
             * lines into a single line
             */
            String[] lines = byteOut.toString().split("(\n\r|\r\n|\n|\r)");

            for (int i = 0; i < lines.length; i++) {
                String sep;
                if (i == 0) {
                    sep = ": ";
                } else {
                    sep = " -> ";
                }

                msgBuf.append(sep).append(lines[i].trim());
            }

            content = msgBuf.toString();
        }

        int contentLen;
        if (content == null) {
            contentLen = 0;
        } else {
            contentLen = content.length();
        }

        if (header.length() + middle.length() + 1 + contentLen < LIVE_MSGMAX) {
            if (contentLen == 0) {
                sendMsg(header + middle + "\n");
            } else {
                sendMsg(header + middle + " " + content + "\n");
            }
        } else {
            String prefix = header + middle + " ";
            while (content.length() > 0) {
                int maxLen = LIVE_MSGMAX - prefix.length();

                String msg;
                if (maxLen > content.length()) {
                    msg = content;
                    content = "";
                } else {
                    msg = content.substring(0, maxLen);
                    content = content.substring(maxLen);
                }

                sendMsg(prefix + msg + "\n");

                // don't add loggerName/threadName to remaining lines
                prefix = header;
            }
        }
    }
}
