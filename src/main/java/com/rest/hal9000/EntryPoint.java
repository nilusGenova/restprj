package com.rest.hal9000;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Path("clock")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clock() {
	return App.registry.getRegisteredObj('C').exposeJsonData();
    }

    @POST
    @Path("setclock")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clockSet() {
	log.info("Setting actual time");
	return App.registry.getRegisteredObj('C').executeCmd("", "");
    }

    @GET
    @Path("thermo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThermo() {
	return App.registry.getRegisteredObj('T').exposeJsonData();
    }

    @POST
    @Path("thermo")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setThermo(@DefaultValue("-1") @QueryParam("required") int tempRequired,
	    @DefaultValue("-1") @QueryParam("hysteresis") int hysteresis) {
	log.info("Setting thermo vals");
	if (tempRequired != -1) {
	    return App.registry.getRegisteredObj('T').executeCmd("R", Integer.toString(tempRequired));
	}
	if (hysteresis != -1) {
	    return App.registry.getRegisteredObj('T').executeCmd("H", Integer.toString(hysteresis));
	}
	return Response.serverError().entity("ERROR: invalid cmd").build();
	// TODO:
    }

    @POST
    @Path("debug")
    @Produces(MediaType.TEXT_PLAIN)
    public Response debugEnabler(@DefaultValue("true") @QueryParam("enable") boolean enable) {
	if (enable) {
	    CommonUtils.setLogLevel("DEBUG");
	    log.debug("Debug enabled");
	    return Response.status(Response.Status.OK).entity("Debug enabled").build();
	} else {
	    log.debug("Debug disabled");
	    CommonUtils.setLogLevel("INFO");
	    return Response.status(Response.Status.OK).entity("Debug disabled").build();
	}
    }

}
