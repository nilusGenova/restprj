package com.rest.hal9000;

import java.util.NoSuchElementException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public final class App {

    private final static int REST_SERVER_PORT = 8080;
    private final static String LOG_PATH_SYS_PROP = "LogPath";
    private final static String DEBUG_LEVEL_SYS_PROP = "DebugLogLevel";
    private final static TwoWaysSerialComms serial = new TwoWaysSerialComms();
    public final static Registry registry = new Registry();
    private final static Parser parser = new Parser((id) -> registry.getRegisteredObj(id));

    public static boolean isSerialConnected() {
	return serial.isConnected();
    }

    // Warning: not covered by tests
    public static Response setTemp(String value) throws Exception {
	ProgramObjAgent prog = (ProgramObjAgent) registry.getRegisteredObj('P');
	ThermoObjAgent thermo = (ThermoObjAgent) registry.getRegisteredObj('T');

	if ((prog != null) && (thermo != null)) {
	    final String mode = prog.getMode().toLowerCase();
	    if (!mode.equals("off")) {
		Response resp;
		if (mode.equals("man_off")) {
		    resp = prog.executeSet("temp_off", value);
		    thermo.executeSet("required", value);
		    return resp;
		} else if (mode.equals("man_on")) {
		    resp = prog.executeSet("temp_on", value);
		    thermo.executeSet("required", value);
		    return resp;
		} else {
		    return thermo.executeSet("required", value);
		}
	    } else {
		return Response.status(Response.Status.BAD_REQUEST).build();
	    }
	} else {
	    throw new NoSuchElementException();
	}
    }

    public static void main(String[] args) throws Exception {
	CommonUtils.setLogPath(System.getProperty(LOG_PATH_SYS_PROP));
	CommonUtils.initialDebugStatus(System.getProperty(DEBUG_LEVEL_SYS_PROP) != null);

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
	System.out.println(LOG_PATH_SYS_PROP + "=" + System.getProperty(LOG_PATH_SYS_PROP));
	System.out.println(DEBUG_LEVEL_SYS_PROP + "=" + System.getProperty(DEBUG_LEVEL_SYS_PROP));
	System.out.println("Ip Address:" + CommonUtils.getMy_CIDR_IpAddress());
	System.out.println("Log:" + CommonUtils.getLoggerFilePath());
	System.out.println("Temperature log:" + CommonUtils.getTempLoggerFilePath());
	System.out.println("Alarm log:" + CommonUtils.getAlarmLoggerFilePath());

	// Inizializza il rest-server
	try {
	    jettyServer.start();
	    jettyServer.join();
	} finally {
	    jettyServer.destroy();
	}
    }
}