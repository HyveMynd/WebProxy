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
    private String verb;
	private String uri;
	private final static int HTTP_PORT = 80;

    public HttpRequest(BufferedReader in){
		try {
			String[] line = in.readLine().split(" ");
			verb = line[0];
			uri = line[1].toLowerCase();
			if (uri.startsWith("http")){
				URL url = new URL(uri);
				addHeader("host", url.getHost());
				uri = url.getPath().length() == 0 ? "/" : url.getPath();
			}
			setVersion(line[2]);
			parseHeaders(in);
		} catch (IOException e) {
			logger.error("Unable to parse request: {}", e);
			return;
		}
		logger.debug("Request Success. Verb: {}, URI: {}, Port: {}", verb, uri, getPort());
    }

	public String getHost(){
		return getHeader("host");
	}

	public String getPath(){
		return uri;
	}

    public String getVerb() {
        return verb;
    }

	public String getUri() {
		return uri;
	}

	public int getPort() {
		String host = getHeader("host");
		if (host != null && host.indexOf(":") > 0){
			return Integer.parseInt(host.split(":")[1]);
		}
		return HTTP_PORT;
	}

	@Override
	public String toString() {
		String request = verb + " " + uri + " " + "HTTP/1.0" + CRLF;
		for (String headerKey : getHeadersKeys()){
			String value = getHeader(headerKey);
			request += headerKey + ": " + value + CRLF;
		}

		// No persistent connections
		request += "Connection: close" + CRLF;
		request += CRLF;
		logger.debug("REQUEST:\n{}", request);
		return request;
	}
}
