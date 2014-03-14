WebProxy
========
A simple WebProxy created for the Univesity of Utah CS4480 Networking Programming course.

Usage:
java -jar [jar-name] [ip] [port]

[jar-name]:
	The name of the WebProxy jar.

[ip]
	A string value representing the ipV4 address to use for the 
	proxy server. Eg. 127.0.0.1
[port]
	An integer value representing the port to accept incoming requests
	for the proxy server. Eg. 60962

Notes:
	The [ip] and [port] values may be ignored, and the system will 
	default to localhost (127.0.0.1) and to port number 60962.

CADE USERS: CADE users must use jdk-1.7. It can be found in /usr/bin/java
            (the default jdk-1.6 is in /usr/local/bin/java)
