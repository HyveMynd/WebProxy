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
    private int port;

    public HttpRequest(BufferedReader from) throws IOException {
        String firstLine = "";
        try {
            firstLine = from.readLine();
        } catch (IOException e) {
            logger.error("Error reading request line.", e);
			return;
        }
		if (firstLine == null){
			throw new IOException("Request terminated early. First line is null.");
		}

        String[] tmp = firstLine.split(" ");
        method = tmp[0]; // method GET
        URI =  tmp[1]; // URI
        setVersion(tmp[2]); // HTTP version
        logger.info("URI is: {}", URI);
        try {
            parseHeaders(from);
            if (URI.startsWith("http")){
				URL url = new URL(URI);
				addHeader("host", url.getHost());
				port = HTTP_PORT;
				URI = url.getPath().length() == 0 ? "/" : url.getPath();
			}
			String host = getHost();
			if (host.indexOf(":") > 0){
				tmp = host.split(":");
				port = Integer.parseInt(tmp[1]);
				addHeader("host", tmp[0]);
			} else {
				port = HTTP_PORT;
			}
		} catch (IOException e) {
            logger.error("Error reading from socket.", e);
        }
    }

    public String getHost() {
        return getHeader("host");
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
		String request = String.format("%s %s %s\r\n", method, URI, getVersion());
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

	public void addConditionalHeader(String lastModified){
		addHeader("if-modified-since", lastModified);
	}
}

