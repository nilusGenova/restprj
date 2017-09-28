package com.rest.hal9000;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry {
    private static final Logger log = LoggerFactory.getLogger(Registry.class);
    private final static List<HalObjAgent> listRegistered = Collections.synchronizedList(new ArrayList<HalObjAgent>());

    public Registry() {
    }

    public HalObjAgent getRegisteredObj(char objId) {
	synchronized (listRegistered) {
	    for (HalObjAgent obj : listRegistered) {
		if (obj.getId() == objId) {
		    return obj;
		}
	    }
	}
	return null;
    }

    public int numOfRegisteredObj() {
	synchronized (listRegistered) {
	    return listRegistered.size();
	}
    }

    public void registerObj(HalObjAgent obj) {
	final char id = obj.getId();
	log.info("Register obj {}", id);
	if (getRegisteredObj(id) != null) {
	    log.error("Object {} already registered", id);
	} else {
	    synchronized (listRegistered) {
		listRegistered.add(obj);
	    }
	}
	log.debug("Num of registered obj:{}", numOfRegisteredObj());
    }
}
