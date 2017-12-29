package com.rest.hal9000;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public final class App {

    private final static int REST_SERVER_PORT = 8080;
    private final static String DEBUG_FILE_NAME = "/tmp/hal9000debug";
    private final static String LOG_PATH_SYS_PROP = "LogPath";
    private final static TwoWaysSerialComms serial = new TwoWaysSerialComms();
    public final static Registry registry = new Registry();
    private final static Parser parser = new Parser((id) -> registry.getRegisteredObj(id));

    public static boolean isSerialConnected() {
	return serial.isConnected();
    }

    public static void main(String[] args) throws Exception {
    	CommonUtils.setLogPath(System.getProperty(LOG_PATH_SYS_PROP));
    	CommonUtils.initialDebugStatus(DEBUG_FILE_NAME);

    	final ClockObjAgent clockAgent = new ClockObjAgent("clock", (msg) -> serial.sendMsg(msg));
    	final ThermoObjAgent thermoAgent = new ThermoObjAgent("thermo", (msg) -> serial.sendMsg(msg));
    	final AlarmObjAgent alarmObjAgent = new AlarmObjAgent("alarm", (msg) -> serial.sendMsg(msg));
    	final ProgramObjAgent programObjAgent = new ProgramObjAgent("program", (msg) -> serial.sendMsg(msg));

    	registry.registerObj(clockAgent);
    	registry.registerObj(thermoAgent);
    	registry.registerObj(alarmObjAgent);
    	registry.registerObj(programObjAgent);

    	parser.start();

    	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    	context.setContextPath("/");

    	Server jettyServer = new Server(REST_SERVER_PORT);
    	jettyServer.setHandler(context);

    	ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
    	jerseyServlet.setInitOrder(0);

    	// Tells the Jersey Servlet which REST service/class to load.
    	jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", EntryPoint.class.getCanonicalName());

    	// Inizializza gestione porta seriale
    	try {
    		serial.startConnectionManager((str) -> parser.msgToBeParsed(str));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	System.out.println("Ip Address:" + CommonUtils.getMy_CIDR_IpAddress());
    	System.out.println("Log:" + CommonUtils.getLoggerFilePath());
    	System.out.println("Temperature log:" + CommonUtils.getTempLoggerFilePath());
    	// Inizializza il rest-server
    	try {
    		jettyServer.start();
    		jettyServer.join();
    	} finally {
    		jettyServer.destroy();
    	}
    }
}