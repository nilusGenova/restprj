package com.rest.hal9000;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hal9000")
public class EntryPoint {

    private static Logger log = LoggerFactory.getLogger(EntryPoint.class);

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
}
