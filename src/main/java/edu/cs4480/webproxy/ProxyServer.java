package edu.cs4480.webproxy;

import edu.cs4480.webproxy.cache.CacheManager;
import edu.cs4480.webproxy.http.HttpRequest;
import edu.cs4480.webproxy.http.HttpResponse;
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
	private static ServerSocket serverSocket;
	private static final ExecutorService threadPool = Executors.newFixedThreadPool(100);

	private static void initDefault() {
		try {
			InetAddress localhost = InetAddress.getByName("127.0.0.1");
			serverSocket = new ServerSocket(DEFAULT_PORT, 0, localhost);
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

	public static void main(String[] args){
		if (args.length == 0){
			logger.info("Default Init");
			initDefault();
		} else {
			init(args);
		}

		while (true){
			try {
				logger.info("Awaiting request...");
				synchronized (threadPool){
					threadPool.execute(new RequestHandler(serverSocket.accept()));
				}
			} catch (IOException e) {
				logger.error("Could not accept request.", e);
			}
		}
	}
}
