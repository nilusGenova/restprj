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

    private Response executeCmdForObj(char obj, String cmd, String prm) {
	try {
	    return App.registry.getRegisteredObj(obj).executeCmd(cmd, prm);
	} catch (Exception e) {
	    log.debug("Failure in {} to execute {} with prm {}", obj, cmd, prm, e);
	    return Response.status(Response.Status.BAD_REQUEST).build();
	}
    }

    private Response createDataForObj(char obj, String cmd, String prm) {
	try {
	    return App.registry.getRegisteredObj(obj).createData(cmd, prm);
	} catch (Exception e) {
	    log.debug("Failure in {} to create {} with prm {}", obj, cmd, prm, e);
	    return Response.status(Response.Status.BAD_REQUEST).build();
	}
    }

    private Response deleteDataForObj(char obj, String cmd, String prm) {
	try {
	    return App.registry.getRegisteredObj(obj).deleteData(cmd, prm);
	} catch (Exception e) {
	    log.debug("Failure in {} to delete {} with prm {}", obj, cmd, prm, e);
	    return Response.status(Response.Status.BAD_REQUEST).build();
	}
    }

    private Response exposeDataForObj(char obj) {
	try {
	    return App.registry.getRegisteredObj(obj).exposeJsonData();
	} catch (Exception e) {
	    log.debug("Failure exposing data of {} ", obj, e);
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
    }

    @GET
    @Path("clock")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clock() {
	return exposeDataForObj('C');
    }

    @POST
    @Path("setclock")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clockSet() {
	log.info("Setting actual time");
	return executeCmdForObj('C', "", "");
    }

    @GET
    @Path("thermo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThermo() {
	return exposeDataForObj('T');
    }

    @POST
    @Path("thermo")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setThermo(@DefaultValue("-1") @QueryParam("required") int tempRequired,
	    @DefaultValue("-1") @QueryParam("hysteresis") int hysteresis) {
	log.info("Setting thermo vals");
	if ((tempRequired != -1) || (hysteresis != -1)) {
	    if (tempRequired != -1) {
		return executeCmdForObj('T', "R", Integer.toString(tempRequired));
	    }
	    if (hysteresis != -1) {
		return executeCmdForObj('T', "H", Integer.toString(hysteresis));
	    }
	}
	return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
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
