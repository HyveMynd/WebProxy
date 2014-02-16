package edu.cs4480.webproxy.cache;

import edu.cs4480.webproxy.http.HttpRequest;
import edu.cs4480.webproxy.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by andresmonroy on 1/22/14.
 */
public class CacheManager {
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class.getName());
	private static Map<String, String> cache = new Hashtable<>();

	public synchronized static void cacheResponse(HttpRequest request, HttpResponse response) throws IOException {

		// Output file based on last modified date
		String url = request.getHost() + request.getUri();
		String filename = String.format("cached_%s", getSanitizedDate(response));
		File cacheFile = new File("cache/");
		if (!cacheFile.exists()){
			cacheFile.mkdirs();
		}
		cacheFile = new File(cacheFile, filename);
		logger.info("Saving cache to file: {}", cacheFile.getAbsolutePath());
		DataOutputStream out = new DataOutputStream(new FileOutputStream(cacheFile));
		out.writeBytes(response.toString());
		out.write(response.getBody());
		out.close();

		// Save cache to map
		cache.put(url, cacheFile.getAbsolutePath());
	}

	private synchronized static String getSanitizedDate(HttpResponse response){
		String date = response.getHeader("Last-modified");
		if (date != null){
			date = date.replaceAll(",", "");
			date = date.replaceAll(" ", "");
			return date.replaceAll(":", "");
		}
		return Long.toString(System.currentTimeMillis());
	}

	public synchronized static boolean cacheExists(HttpRequest request){
		return cache.get(request.getHost() + request.getUri()) != null;
	}

	public synchronized static byte[] getCachedResponse(HttpRequest request) throws IOException {

		// Get file based on value of url key
		byte[] bytesCached = new byte[0];
		String url = request.getHost() + request.getUri();
		String path = cache.get(url);
		if (path != null){
			logger.info("Cache hit on: {}", url);
			logger.info("Getting file cached at: {}", path);
			File file = new File(path);
			FileInputStream in = new FileInputStream(file);
			bytesCached = new byte[(int)file.length()];
			IOUtils.read(in, bytesCached);
		} else {
			logger.error("Cache miss at: {}\nFile: {}", url, path);
		}
		return bytesCached;
	}

}
