package com.rest.hal9000;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class App {

    private final static String usbDevice = "/dev/ttyUSB0";
    private final static String DEBUG_FILE_NAME = "/tmp/hal900debug";

    public static void main(String[] args) throws Exception {
	CommonUtils.initialDebugStatus(DEBUG_FILE_NAME);
	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	context.setContextPath("/");

	Server jettyServer = new Server(8080);
	jettyServer.setHandler(context);

	ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
	jerseyServlet.setInitOrder(0);

	// Tells the Jersey Servlet which REST service/class to load.
	jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", EntryPoint.class.getCanonicalName());

	// Inizializza gestione porta seriale
	try {
	    TwoWaysSerialComms serial = new TwoWaysSerialComms();
	    serial.connect(usbDevice, (str) -> System.out.println("*" + str + "*"));

	    Thread.sleep(5000);
	    serial.sendMsg("GCE");
	    Thread.sleep(2000);
	    serial.sendMsg("GCD");
	    serial.sendMsg("GCE");
	    serial.sendMsg("GCD");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    jettyServer.start();
	    jettyServer.join();
	} finally {
	    jettyServer.destroy();
	}
    }
}