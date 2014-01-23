package edu.cs4480.webproxy.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by andresmonroy on 1/21/14.
 */
public abstract class Http {
	protected static final String CRLF = "\r\n";
	private Map<String, String> headers;
	private String version;

	public String getHeader(String headerKey){
		try{
			return headers.get(headerKey.toUpperCase());
		} catch(NullPointerException e){
			return null;
		}
	}

	protected void addHeader(String headerKey, String header){
		if (headers == null){
			headers = new HashMap<String, String>();
		}
		headers.put(headerKey.toUpperCase(), header);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	protected void parseHeaders(BufferedReader in) throws IOException {
		String line = in.readLine();
		while(line.length() != 0){
			String[] header = line.split(" ");
			addHeader(header[0].replace(":", "").toUpperCase(), header[1]);
			line = in.readLine();
		}
	}

	public Set<String> getHeadersKeys(){
		return headers.keySet();
	}
}
