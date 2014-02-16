package edu.cs4480.webproxy.cache;

import edu.cs4480.webproxy.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by andresmonroy on 1/22/14.
 */
public class CacheManager {
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class.getName());
	private static final String INDEX = "homepage";
	private static final String CACHE_SUFFIX = "";

	public static void cacheResponse(HttpResponse response, String host, String path) throws IOException {
		// Create the directories if they do not exist
		if (!createCacheDirectories(host)){
			throw new IOException("Could not create cache directories");
		}
		String dir = getCacheFilePath(host, path);

		// Write to file
		IOUtils.write(response.toString() + new String(response.getBody()), new FileOutputStream(dir), "utf-8");
	}

	public static HttpResponse getCachedResponse(String host, String path) throws IOException {
		// Read from file
		String filepath = getCacheFilePath(host, path);
		byte[] content = new byte[HttpResponse.MAX_OBJECT_SIZE];
		IOUtils.read(new FileInputStream(filepath), content);
		return new HttpResponse(new DataInputStream(new ByteArrayInputStream(content)));
	}

	public static boolean cacheExists(String host, String path){
		String dir = getCacheFilePath(host, path);
		return new File(dir).exists();
	}

	private static String getCacheFilePath(String host, String path){
		String dir = getDirectoryPath(host);
		path = sanitizePath(path);
		return dir + path + CACHE_SUFFIX;
	}

	private static String getDirectoryPath(String host){
		// Get cache directory paths
		String dir = System.getProperty("user.dir");
		logger.debug("Working directory: {}", dir);
		dir = dir + String.format("/cache/%s/", host);
		logger.info("Cache directory: {}", dir);
		return dir;
	}

	private static boolean createCacheDirectories(String host){
		String dir = getDirectoryPath(host);
		File file = new File(dir);
		if (!file.exists()){
			file.mkdirs();
		}
		return file.exists();
	}

	private static String sanitizePath(String path){
		return (path.equals("/")) ? INDEX : path;
	}
}
