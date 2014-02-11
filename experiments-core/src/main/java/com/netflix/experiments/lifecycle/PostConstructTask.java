package com.netflix.experiments.lifecycle;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostConstructTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PostConstructTask.class);
    
    private final Object obj;
    private final Method method;
    
    public PostConstructTask(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
    }
    
    @Override
    public void run() {
        LOG.info("@PostConstruct : " + obj.getClass().getCanonicalName() + " " + method.getName());
        try {
            method.invoke(obj, (Object[])null);
        } catch (Exception e) {
            LOG.error("Error calling @PostConstruct method " + obj.getClass().getCanonicalName() + " " + method.getName());
            throw new RuntimeException(e);
        }
    }

}
