package edu.cs4480.webproxy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;

/**
 * Created by andresmonroy on 1/21/14.
 */
public class HttpResponse extends Http{
	private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class.getName());
	public static final int MAX_SIZE = 100000;
	private static final int BUF_SIZE = 8192;
	private char[] body;
	private String statusCode;
	private String statusMsg;

	public static String getErrorResponse(int code, String msg){
		return String.format("HTTP/1.1 %d %s" + CRLF, code, msg);
	}

	private HttpResponse(){
		body = new char[MAX_SIZE];
	}

	public HttpResponse(char[] content){
		this(new BufferedReader(new CharArrayReader(content)));
	}

	public HttpResponse(BufferedReader in){
		this();
		try {
			String[] line = in.readLine().split(" ");
			setVersion(line[0]);
			statusCode = line[1];
			statusMsg = line[2];
			parseHeaders(in);
			parseBody(in);
		} catch (IOException e) {
			logger.error("Unable to parse response: {}", e);
			return;
		}
		logger.debug("Response success. StatusCode: {}, MSG: {}", statusCode, statusMsg);
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
		response += CRLF;
		logger.debug("RESPONSE:\n{}", response);
		return response;
	}
}
