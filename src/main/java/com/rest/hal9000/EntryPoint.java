package com.rest.hal9000;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hal9000")
public class EntryPoint {

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test riuscito";
    }
    
    @GET
    @Path("termometer")
    @Produces(MediaType.TEXT_PLAIN)
    public String termometer() {
        return "15";
    }
}

