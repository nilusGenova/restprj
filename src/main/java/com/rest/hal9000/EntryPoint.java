package com.rest.hal9000;

import java.util.NoSuchElementException;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hal9000")
public class EntryPoint {

    private static final Logger log = LoggerFactory.getLogger(EntryPoint.class);

    private HalObjAgent getRegisteredObjFromPath(String path) throws Exception {
	log.debug("Search obj from path:{}", path);
	final char objId = Character.toUpperCase(path.charAt(0));
	HalObjAgent obj = App.registry.getRegisteredObj(objId);
	if ((obj != null) && (obj.getPathName().equals(path))) {
	    return obj;
	}
	throw new NoSuchElementException();
    }

    @GET
    @Path("{object}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOnObject(@PathParam("object") String path, @QueryParam("a") String attribute) {
	try {
	    if (attribute == null) {
		return getRegisteredObjFromPath(path).exposeJsonData();
	    } else {
		return getRegisteredObjFromPath(path).exposeJsonAttribute(attribute.toLowerCase());
	    }
	} catch (NoSuchElementException e) {
	    log.debug("Not Found path:{} attr:{}", path, attribute);
	    return Response.status(Response.Status.NOT_FOUND).build();
	} catch (Exception e) {
	    log.debug("Failure exposing data of {} attr:{}", path, attribute, e);
	    return Response.status(Response.Status.BAD_REQUEST).build();
	}
    }

    @POST
    @Path("{object}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response postOnObject(@PathParam("object") String path, @QueryParam("a") String attribute,
	    @QueryParam("v") String value) {
	try {
	    return getRegisteredObjFromPath(path).executeSet(attribute.toLowerCase(), value);
	} catch (NoSuchElementException e) {
	    log.debug("Not Found path:{} attr:{}", path, attribute);
	    return Response.status(Response.Status.NOT_FOUND).build();
	} catch (Exception e) {
	    log.debug("Failure in {} to set attr:{} with val:{}", path, attribute, value, e);
	    return Response.status(Response.Status.BAD_REQUEST).build();
	}
    }

    @DELETE
    @Path("{object}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteOnObject(@PathParam("object") String path, @QueryParam("c") String cmd,
	    @QueryParam("p") String prm) {
	try {
	    return getRegisteredObjFromPath(path).deleteData(cmd.toLowerCase(), prm);
	} catch (NoSuchElementException e) {
	    log.debug("Not Found path:{} cmd:{}", path, cmd);
	    return Response.status(Response.Status.NOT_FOUND).build();
	} catch (Exception e) {
	    log.debug("Failure in {} to delete cmd:{} with prm:{}", path, cmd, prm, e);
	    return Response.status(Response.Status.BAD_REQUEST).build();
	}
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
