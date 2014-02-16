package edu.cs4480.webproxy.http;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by andresmonroy on 1/21/14.
 */
public class HttpResponse extends Http{
	private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class.getName());
    public final static int MAX_OBJECT_SIZE = 500000;
    private String statusLine = "";
    private byte[] body = new byte[MAX_OBJECT_SIZE];

    public HttpResponse(DataInputStream fromServer) {
		try {
			statusLine = fromServer.readLine();
			parseHeaders(fromServer);
		} catch (IOException e) {
			logger.error("Error while parsing response header.", e);
			return;
		}

		try {
			IOUtils.read(fromServer, body);
		} catch (IOException e) {
			logger.error("Error reading response body.", e);
		}
    }

    public byte[] getBody() {
        return body;
    }

	public String getLastModified(){
		String date = getHeader("LAST-MODIFIED");
		return (date == null) ? "" : date;
	}

	public int getStatusCode(){
		return Integer.parseInt(statusLine.split(" ")[1]);
	}

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
