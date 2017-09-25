package com.rest.hal9000;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hal9000")
public class EntryPoint {

    private static final Logger log = LoggerFactory.getLogger(EntryPoint.class);

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
	log.info("test called");
	return "Test riuscito";
    }

    @GET
    @Path("termometer")
    @Produces(MediaType.TEXT_PLAIN)
    public String termometer() {
	log.info("termometer called");
	return "15";
    }

    @GET
    @Path("debug")
    @Produces(MediaType.TEXT_PLAIN)
    public String debugEnabler(@DefaultValue("true") @QueryParam("enable") boolean enable) {
	if (enable) {
	    CommonUtils.setLogLevel("DEBUG");
	    log.debug("Debug enabled");
	    return "Debug enabled";
	} else {
	    log.debug("Debug disabled");
	    CommonUtils.setLogLevel("INFO");
	    return "Debug disabled";
	}
    }

}
