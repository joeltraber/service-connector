package org.serviceconnector.ctrl.util;

public class TestConstants {
	public static final String LOCALHOST = "localhost";
	public static final String HOST = "localhost";
	public static final int PORT_HTTP = 7000;
	public static final int PORT_TCP = 9000;
	public static final int PORT_MIN = 1;
	public static final int PORT_MAX = 65535;
	public static final int PORT_LISTENER = 30000; 

	public static final String log4jSrvProperties = "log4j-srv.properties";

	public static final String log4jSCProperties = "log4j-sc.properties";
	public static final String log4jSC1Properties = "log4j-sc1.properties";		//cascaded configuration
	public static final String log4jSC2Properties = "log4j-sc2.properties";		//cascaded configuration
	
	public static final String scProperties0 = "scIntegration.properties";
	public static final String scProperties1 = "scIntegrationChanged.properties";
	public static final String sc1Properties = "sc1.properties";
	public static final String sc2Properties = "sc2.properties";
	
	public static final String serviceNameSession = "local-session-service";
	public static final String serviceNameAlt = "sc1-session-service";
	public static final String serviceNameSessionDisabled = "disabledService";
	public static final String serviceNamePublish = "local-publish-service";
	public static final String serviceNamePublishDisabled = "disabledPublish";
	
	public static final String pangram = "The quick brown fox jumps over a lazy dog.";
	public static final String stringLength32 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength33 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength257 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
	public static final int dataLength60kB = 61440;
	public static final int dataLength1MB = 1048576;
	
	public static final String pidLogFile = "pid.log";
	
	public static final String sessionSrv = "session";
	public static final String publishSrv = "publish";
	
	public static final String mask = "0000121ABCDEFGHIJKLMNO-----------X-----------";

}
