package edu.cs4480.webproxy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by andresmonroy on 1/21/14.
 */
public class HttpResponse extends Http{
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class.getName());
	private static final int MAX_SIZE = 100000;
	private static final int BUF_SIZE = 8192;
	private char[] body;
	private String statusCode;
	private String statusMsg;

	public HttpResponse(BufferedReader in){
		body = new char[MAX_SIZE];
		try {
			String[] line = in.readLine().split(" ");
			setVersion(line[0]);
			statusCode = line[1];
			statusMsg = line[2];
			parseHeaders(in);
			parseBody(in);
		} catch (IOException e) {
			log.error("Unable to parse response: {}", e);
			return;
		}
		log.debug("Response success. StatusCode: {}, MSG: {}", statusCode, statusMsg);
	}

	private void parseBody(BufferedReader in) throws IOException{
		int contentLength = -1;
		int bytesRead = 0;
		int offset = 0;
		if (getHeader("content-length") != null){
			contentLength = Integer.parseInt(getHeader("content-length"));
		}
		while(bytesRead < contentLength && bytesRead != -1 && offset < MAX_SIZE){
			bytesRead = in.read(body, offset, BUF_SIZE);
			offset = bytesRead;
		}
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public char[] getBody() {
		return body;
	}

	@Override
	public String toString() {
		String response = getVersion() + " " + statusCode + " " + statusMsg + CRLF;
		for (String headerKey : getHeadersKeys()){
			String value = getHeader(headerKey);
			response += headerKey + ": " + value + CRLF;
		}
		return response;
	}
}
