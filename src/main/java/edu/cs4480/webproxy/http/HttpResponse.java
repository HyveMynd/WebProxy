package edu.cs4480.webproxy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by andresmonroy on 1/21/14.
 */
public class HttpResponse extends Http{
	private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class.getName());

    /** How big is the buffer used for reading the object */
    private final static int BUF_SIZE = 8192;

    /** Maximum size of objects that this proxy can handle. For the
     * moment set to 100 KB. You can adjust this as needed. */
    public final static int MAX_OBJECT_SIZE = 500000;

    private String statusLine = "";
//    private String headers = "";
    private byte[] body = new byte[MAX_OBJECT_SIZE];

    public HttpResponse(DataInputStream fromServer) {
	/* Length of the object */
        int length = -1;
        boolean gotStatusLine = false;
        BufferedReader in = new BufferedReader(new InputStreamReader(fromServer));

        try {

            String[] line = in.readLine().split(" ");
            setVersion(line[0]);
            statusLine = line[2];
            parseHeaders(in);

//            String line = reader.readLine();
//            while (line.length() != 0) {
//                if (!gotStatusLine) {
//                    statusLine = line;
//                    gotStatusLine = true;
//                } else {
////                    headers += line + CRLF;
//                    parseHeaders(reader);
//                }
////                if (line.startsWith("Content-Length:") ||
////                        line.startsWith("Content-length:")) {
////                    String[] tmp = line.split(" ");
////                    length = Integer.parseInt(tmp[1]);
////                }
//                length = Integer.parseInt(getHeader("content-length"));
//                line = reader.readLine();
//            }
        } catch (IOException e) {
            logger.error("Error while reading from response.", e);
        }

        try {
            int bytesRead = 0;
            byte buf[] = new byte[BUF_SIZE];
            boolean loop = false;

            if (length == -1) {
                loop = true;
            }
            while (bytesRead < length || loop) {
                int res = fromServer.read(buf, 0, BUF_SIZE);
                if (res == -1) {
                    break;
                }
                for (int i = 0;
                     i < res && (i + bytesRead) < MAX_OBJECT_SIZE;
                     i++) {
                    body[bytesRead + i] = buf[i];
                }
                bytesRead += res;
            }
        } catch (IOException e) {
            logger.error("Error reading response body.", e);
            return;
        }


    }

    public int getStatusCode() {
        return Integer.parseInt(statusLine.split(" ")[0]);
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        String response = statusLine + CRLF;
        for (String headerKey : getHeadersKeys()){
            String value = getHeader(headerKey);
            response += headerKey + ": " + value + CRLF;
        }
        response += CRLF;
        logger.trace("RESPONSE:\n{}", response);
        return response;
    }
}
