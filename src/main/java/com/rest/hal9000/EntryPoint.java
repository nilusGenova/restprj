package com.rest.hal9000;

import java.io.File;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hal9000")
public class EntryPoint {

    private static final String LOGGER_FILE = "/tmp/temperatures.log";
    private static final String DWNLD_LOGGER_FILE = "temperatures.csv";

    private static final String ACCESS_NOT_ALLOWED = "Access Not Allowed";

    private static final Logger log = LoggerFactory.getLogger(EntryPoint.class);

    @Context
    private HttpServletRequest request;

    private HalObjAgent getRegisteredObjFromPath(String path) throws Exception {
	log.debug("Search obj from path:{}", path);
	final char objId = Character.toUpperCase(path.charAt(0));
	HalObjAgent obj = App.registry.getRegisteredObj(objId);
	if ((obj != null) && (obj.getPathName().equals(path))) {
	    return obj;
	}
	throw new NoSuchElementException();
    }

    private boolean isAccessAllowed() {
	if (request != null) {
	    final String remoteAddr = request.getRemoteAddr();
	    if ("127.0.0.1".equals(remoteAddr)) {
		log.debug("Http request from:{}", remoteAddr);
		return true;
	    } else {
		log.info("Not allowed access from:{}", remoteAddr);
	    }
	}
	return false;
    }

    @GET
    @Path("connected")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkConnection() {
	if (!isAccessAllowed()) {
	    return ACCESS_NOT_ALLOWED;
	}
	return App.isSerialConnected() ? "YES" : "NO";

    }

    @GET
    @Path("logger")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() {
	if (!isAccessAllowed()) {
	    return Response.status(Response.Status.FORBIDDEN).build();
	}
	File file = new File(LOGGER_FILE);
	ResponseBuilder response = Response.ok((Object) file);
	response.header("Content-Disposition", "attachment; filename=" + DWNLD_LOGGER_FILE);
	return response.build();
    }

    @GET
    @Path("{object}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOnObject(@PathParam("object") String path, @QueryParam("a") String attribute) {
	if (!isAccessAllowed()) {
	    return Response.status(Response.Status.FORBIDDEN).build();
	}
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
	if (!isAccessAllowed()) {
	    return Response.status(Response.Status.FORBIDDEN).build();
	}
	try {
	    if (value == null) {
		value = "";
	    }
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
	if (!isAccessAllowed()) {
	    return Response.status(Response.Status.FORBIDDEN).build();
	}
	try {
	    if (prm == null) {
		prm = "";
	    }
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
	if (!isAccessAllowed()) {
	    return ACCESS_NOT_ALLOWED;
	}
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
