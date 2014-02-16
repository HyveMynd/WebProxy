package edu.cs4480.webproxy;

import edu.cs4480.webproxy.cache.CacheManager;
import edu.cs4480.webproxy.http.HttpRequest;
import edu.cs4480.webproxy.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andresmonroy on 1/22/14.
 */
public class ProxyServer {

	private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class.getName());
	private static final int DEFAULT_PORT = 60962;
	private static final int MAX_THREADS = 100;
	private static ServerSocket serverSocket;
	private static ExecutorService threadPool;

	private static void initDefault() {
		try {
			InetAddress localhost = InetAddress.getByName("127.0.0.1");
			serverSocket = new ServerSocket(DEFAULT_PORT, 0, localhost);
			threadPool = Executors.newFixedThreadPool(MAX_THREADS);
		} catch (IOException e) {
			logger.error("Could not open socket with host {}: {}", DEFAULT_PORT, e);
			System.exit(1);
		}
	}

	private static void init(String[] args){
		try {
			int port = Integer.parseInt(args[1]);
			String ipAddress = args[0];
			logger.debug("Init with port {} and ip {}", port, ipAddress);
			serverSocket = new ServerSocket(port, 0, InetAddress.getByName(ipAddress));
			threadPool = Executors.newFixedThreadPool(MAX_THREADS);
		} catch (ArrayIndexOutOfBoundsException e){
			logger.error("Must include a port number");
			System.exit(1);
		} catch (NumberFormatException e){
			logger.error("Port number must be a positive integer");
			System.exit(1);
		} catch (UnknownHostException e) {
			logger.error("Could not find address {}: {}", args[0], e);
			System.exit(1);
		} catch (IOException e) {
			logger.error("An error occurred while initializing server.", e);
			System.exit(1);
		}
	}

	private static void sendCachedResponse(Socket client, HttpRequest request) throws IOException {
		// Get cached response and send response object
		HttpResponse response = CacheManager.getCachedResponse(request.getHost(), request.getUri());
		sendResponseToClient(client, response);
	}

	private static void sendUnCachedResponse(Socket client, HttpRequest request) throws IOException {
		// Open socket to destination server and send the request
        Socket destination = null;
        try {
		destination = new Socket(request.getHost(), request.getPort());
		logger.debug("Sending request to the destination server");
		new DataOutputStream(destination.getOutputStream()).writeBytes(request.toString());
        } catch (UnknownHostException e){
            logger.error("Unknown host {}: {}", request.getHost(), e.getMessage());
            return;
        }

		// Read response from destination server and send to client
		logger.debug("Reading response from destination server");
		HttpResponse response = new HttpResponse(new DataInputStream(destination.getInputStream()));
		sendResponseToClient(client, response);

		// Cache response
		if (response.getStatusCode() == 200){
			logger.info("Caching response");
			CacheManager.cacheResponse(response, request.getHost(), request.getUri());
		}

		// Close sockets
		logger.debug("Closing sockets");
		destination.close();
	}

	private static void sendResponseToClient(Socket client, HttpResponse response) throws IOException {
		// Send response to client
		logger.debug("Sending response to client");
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeBytes(response.toString());
		out.write(response.getBody());
	}

	public static void handleRequest(Socket client){
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
			if (CacheManager.cacheExists(request.getHost(), request.getUri())){
				logger.info("Sending cached response...");
				sendCachedResponse(client, request);
			} else {
				logger.info("Cached response does not exist. Requesting from destination server");
				sendUnCachedResponse(client, request);
			}
			client.close();
		} catch (IOException e) {
			logger.error("An error occurred while handling a request.", e);
		}
	}

	public static void main(String[] args){
		if (args.length == 0){
			logger.debug("Default Init");
			initDefault();
		} else {
			init(args);
		}
		while (true){
			try {
				logger.info("Awaiting request...");
				threadPool.execute(new RequestHandler(serverSocket.accept()));
			} catch (IOException e) {
				logger.error("Could not accept request.", e);
			}
		}
	}

	static class RequestHandler implements Runnable{
		private Socket client;

		public RequestHandler(Socket client){
			this.client = client;
		}

		@Override
		public void run() {
			try{
				logger.info("Request accepted. Handling request.");
				handleRequest(client);
			} catch (Exception e){
				logger.error("Request failed. Sending error.");
			}
		}
	}
}
