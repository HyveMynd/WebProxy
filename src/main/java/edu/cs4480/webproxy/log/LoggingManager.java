package edu.cs4480.webproxy.log;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by andresmonroy on 1/22/14.
 */
public class LoggingManager {

	public static void appendConsoleLogger(Level level){
		Logger root = Logger.getRootLogger();
		root.addAppender(new ConsoleAppender());
		root.setLevel(level);
	}
}
