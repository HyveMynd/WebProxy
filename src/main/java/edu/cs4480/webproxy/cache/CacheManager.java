package edu.cs4480.webproxy.cache;

import edu.cs4480.webproxy.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by andresmonroy on 1/22/14.
 */
public class CacheManager {
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class.getName());

	public static void cacheResponse(HttpResponse response, String host, String path) throws IOException {
		File file = getCacheFile(host, path);

		// Write to file
		if (file.mkdirs()){
			IOUtils.write(response.toString() + new String(response.getBody()), new FileWriter(file.getAbsoluteFile()));
		} else {
			logger.error("Could not cache response. Failed to create directories");
		}
	}

	public static HttpResponse getCachedResponse(String host, String path) throws IOException {
		// Get cache directory paths
		File file = getCacheFile(host, path);

		// Read from file
		char[] content = new char[HttpResponse.MAX_SIZE];
		IOUtils.read(new FileReader(file), content);
		return new HttpResponse(content);
	}

	public static boolean cacheExists(String host, String path){
		return getCacheFile(host, path).exists();
	}

	private static File getCacheFile (String host, String path) {
		// Get cache directory paths
		String dir = System.getProperty("user.dir");
		logger.info("Working directory: {}", dir);
		String cacheDir = dir + String.format("/cache/%s/%s", host, path);
		logger.info("Cache directory: {}", cacheDir);
		return new File(cacheDir);
	}

}
