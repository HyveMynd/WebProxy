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
    private final static int HTTP_PORT = 80;

    private String method;
    private String URI;
    private String host;
    private int port;

    /** Create HttpRequest by reading it from the client socket */
    public HttpRequest(BufferedReader from) {
        String firstLine = "";
        try {
            firstLine = from.readLine();
        } catch (IOException e) {
            logger.error("Error reading request line.", e);
        }

        String[] tmp = firstLine.split(" ");
        method = tmp[0]; // method GET
        URI =  tmp[1]; // URI
        setVersion(tmp[2]); // HTTP version
        logger.debug("URI is: {}", URI);
        try {
            parseHeaders(from);
            host = getHeader("host");
			if (host != null){
				tmp = host.split(" ");
				if (tmp[1].indexOf(':') > 0) {
					String[] tmp2 = tmp[1].split(":");
					host = tmp2[0];
					port = Integer.parseInt(tmp2[1]);
				} else {
					host = tmp[1];
					port = HTTP_PORT;
				}
			} else {
				URL url = new URL(URI);
				host = url.getHost();
				URI = url.getPath().length() == 0 ? "/" : url.getPath();
				port = HTTP_PORT;
			}
        } catch (IOException e) {
            logger.error("Error reading from socket.", e);
            return;
        }
        logger.debug("Host to contact is: {} at port {}", host, port);
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

