package edu.cs4480.webproxy;


import edu.cs4480.webproxy.cache.CacheManager;
import edu.cs4480.webproxy.http.HttpRequest;
import edu.cs4480.webproxy.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by andresmonroy on 2/15/14.
 */
public class RequestHandler implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private final Socket client;

	public RequestHandler(Socket client){
		this.client = client;
	}

	@Override
	public void run() {
		handleRequest();
	}

	public void handleRequest(){
		try {
			// Parse request
			logger.debug("Parsing request from client");
			HttpRequest request = new HttpRequest(new BufferedReader(new InputStreamReader(client.getInputStream())));
			if (!request.getVerb().equalsIgnoreCase("get")){
				logger.error("Can only handle GET verb");
				return;
			}
			logger.info("Opening a connection to the destination server: {}:{}", request.getHost(), request.getPort());

			// Return the cached response if it exists
			if (CacheManager.cacheExists(request)){
				logger.debug("Sending cached response...");
				sendCachedResponse(client, request);
			} else {
				logger.debug("Cached response does not exist. Requesting from destination server");
				sendUnCachedResponse(client, request);
			}
			client.close();
		} catch (IOException e) {
			logger.error("An error occurred while handling a request.", e);
		}
	}

	private void sendUnCachedResponse(Socket client, HttpRequest request) throws IOException {
		// Open socket to destination server and send the request
		Socket destination = null;
		try {
			destination = new Socket(request.getHost(), request.getPort());
			logger.debug("Sending request to the destination server");
			DataOutputStream out = new DataOutputStream(destination.getOutputStream());
			out.writeBytes(request.toString());
		} catch (UnknownHostException e){
			logger.error("Unknown host {}: {}", request.getHost(), e.getMessage());
			return;
		}

		// Read response from destination server and send to client
		logger.debug("Reading response from destination server");
		HttpResponse response = new HttpResponse(new DataInputStream(destination.getInputStream()));
		sendResponseToClient(client, response);

		// Cache response
		logger.debug("Caching response");
		CacheManager.cacheResponse(request, response);

		// Close sockets
		logger.debug("Closing sockets");
		destination.close();
	}


	private void sendCachedResponse(Socket client, HttpRequest request) throws IOException {
		// Get cached response and send response object
		HttpResponse response = new HttpResponse(
				new DataInputStream(new ByteArrayInputStream(CacheManager.getCachedResponse(request))));
		sendResponseToClient(client, response);
	}

	private void sendResponseToClient(Socket client, HttpResponse response) throws IOException {
		// Send response to client
		logger.debug("Sending response to client");
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeBytes(response.toString());
		out.write(response.getBody());
	}
}
