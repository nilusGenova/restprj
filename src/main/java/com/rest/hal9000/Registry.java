package com.rest.hal9000;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry {
    private static final Logger log = LoggerFactory.getLogger(Registry.class);
    private final static Map<Character, HalObjAgent> registeredMap = new HashMap();

    public Registry() {
    }

    public HalObjAgent getRegisteredObj(char objId) {
	synchronized (registeredMap) {
	    return registeredMap.get(objId);
	}
    }

    public int numOfRegisteredObj() {
	synchronized (registeredMap) {
	    return registeredMap.size();
	}
    }

    public void registerObj(HalObjAgent obj) {
	final char id = obj.getId();
	log.info("Register obj {}", id);
	if (getRegisteredObj(id) != null) {
	    log.error("Object {} already registered", id);
	} else {
	    synchronized (registeredMap) {
		registeredMap.put(id, obj);
	    }
	}
	log.debug("Num of registered obj:{}", numOfRegisteredObj());
    }
}
