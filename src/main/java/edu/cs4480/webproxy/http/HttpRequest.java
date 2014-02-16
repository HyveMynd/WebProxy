package edu.cs4480.webproxy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

/**
 * Created by andresmonroy on 1/21/14.
 */
public class HttpRequest extends Http{
	private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class.getName());
    /** Help variables */
    private final static int HTTP_PORT = 80;

    private String method;
    private String URI;
//    private String headers = "";
    private String host;
    private int port;

    /** Create HttpRequest by reading it from the client socket */
    public HttpRequest(BufferedReader from) {
        String firstLine = "";
        try {
            firstLine = from.readLine();
        } catch (IOException e) {
            System.out.println("Error reading request line: " + e);
        }

        String[] tmp = firstLine.split(" ");

        method = tmp[0]; /* method GET */
        URI =  tmp[1]; /* URI */
        setVersion(tmp[2]); /* HTTP version */

        System.out.println("URI is: " + URI);

        if (!method.equals("GET")) {
            System.out.println("Error: Method not GET");
        }
        try {
            parseHeaders(from);
//            String line = from.readLine();
//            while (line.length() != 0) {
//                headers += line + CRLF;
//                if (line.startsWith("Host:")) {
//                    tmp = line.split(" ");
//                    if (tmp[1].indexOf(':') > 0) {
//                        String[] tmp2 = tmp[1].split(":");
//                        host = tmp2[0];
//                        port = Integer.parseInt(tmp2[1]);
//                    } else {
//                        host = tmp[1];
//                        port = HTTP_PORT;
//                    }
//                }
//                line = from.readLine();
//            }
            host = getHeader("host");
            tmp = host.split(" ");
            if (tmp[1].indexOf(':') > 0) {
                String[] tmp2 = tmp[1].split(":");
                host = tmp2[0];
                port = Integer.parseInt(tmp2[1]);
            } else {
                host = tmp[1];
                port = HTTP_PORT;
            }
        } catch (IOException e) {
            logger.error("Error reading from socket.", e);
            return;
        }
        logger.debug("Host to contact is: " + host + " at port " + port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUri() {
        return URI;
    }

    public String getVerb() {
        return method;
    }

//    public String toString() {
//        String req = "";
//
//        req = method + " " + URI + " " + version + CRLF;
//        req += headers;
//	/* This proxy does not support persistent connections */
//        req += "Connection: close" + CRLF;
//        req += CRLF;
//
//        return req;
//    }

    @Override
    public String toString() {
        String request = method + " " + URI + " " + "HTTP/1.0" + CRLF;
        for (String headerKey : getHeadersKeys()){
            String value = getHeader(headerKey);
            request += headerKey + ": " + value + CRLF;
        }

        // No persistent connections
        request += "Connection: close" + CRLF;
        request += CRLF;
        logger.trace("REQUEST:\n{}", request);
        return request;
    }
}

