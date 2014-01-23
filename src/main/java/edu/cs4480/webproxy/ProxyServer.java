package edu.cs4480.webproxy;

import edu.cs4480.webproxy.cache.CachingManager;
import edu.cs4480.webproxy.http.HttpRequest;
import edu.cs4480.webproxy.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

	public static void handleRequest(Socket client){
		Socket destinationServer;
		HttpRequest request;
		HttpResponse response;
		try {
			logger.debug("Parsing request from client");
			request = new HttpRequest(new BufferedReader(new InputStreamReader(client.getInputStream())));
			logger.debug("Opening a connection to the destination server: {}:{}", request.getHost(), request.getPort());
			destinationServer = new Socket(request.getHost(), request.getPort());
			logger.debug("Sending request to the destination server");
			IOUtils.write(request.toString(), destinationServer.getOutputStream());
			logger.debug("Reading response from destination server");
			response = new HttpResponse(new BufferedReader(new InputStreamReader(destinationServer.getInputStream())));
			logger.debug("Sending response to client");
			OutputStream out = client.getOutputStream();
			IOUtils.write(response.toString(), out);
			IOUtils.write(response.getBody(), out);
			logger.debug("Caching response");
			CachingManager.cacheResponse(response.getBody());
			logger.debug("Closing sockets");
			client.close();
			destinationServer.close();
		} catch (IOException e) {
			logger.error("An error occurred while handling a request.", e);
		}
	}

	public static void main(String[] args){
		if (args.length == 0){
			initDefault();
		} else {
			init(args);
		}
		while (true){
			try {
				threadPool.execute(new RequestHandler(serverSocket.accept()));
			} catch (IOException e) {
				logger.error("Could not accept request.", e);
				continue;
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
				handleRequest(client);
			} catch (Exception e){
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
