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
	private static Map<String, CacheFile> cache = new Hashtable<>();

	public synchronized static void cacheResponse(HttpRequest request, HttpResponse response) throws IOException {

		// Output file based on last modified date
		String url = request.getHost() + request.getUri();
		File file = new File("cache/");
		String filename = Long.toString(System.currentTimeMillis());
		if (!file.exists()){
			file.mkdirs();
		}
		file = new File(file, filename);
		logger.info("Saving cache to file: {}", file.getAbsolutePath());
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		out.writeBytes(response.toString());
		out.write(response.getBody());
		out.close();

		// Save cache to map
		cache.put(url, new CacheFile(file.getAbsolutePath(), response.getLastModified()));
	}

	public synchronized static boolean cacheExists(HttpRequest request){
		return cache.get(request.getHost() + request.getUri()) != null;
	}

	public synchronized static String getLastModified(HttpRequest request){
		return cache.get(request.getHost() + request.getUri()).lastModified;
	}

	public synchronized static byte[] getCachedResponse(HttpRequest request) throws IOException {

		// Get file based on value of url key
		byte[] bytesCached = new byte[0];
		String url = request.getHost() + request.getUri();
		CacheFile cacheFile = cache.get(url);
		if (cacheFile != null){
			logger.info("Cache hit on: {}", url);
			logger.info("Getting file cached at: {}", cacheFile.filePath);
			File file = new File(cacheFile.filePath);
			FileInputStream in = new FileInputStream(file);
			bytesCached = new byte[(int)file.length()];
			IOUtils.read(in, bytesCached);
		} else {
			logger.error("Cache miss at: {}\n", url);
		}
		return bytesCached;
	}

	static class CacheFile{
		String filePath;
		String lastModified;

		public CacheFile(String filePath, String lastModified){
			this.filePath = filePath;
			this.lastModified = lastModified;
		}
	}
}
