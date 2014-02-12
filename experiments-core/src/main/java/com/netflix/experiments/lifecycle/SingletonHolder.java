package com.netflix.experiments.lifecycle;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.experiments.DependencyDestroyer;

public class SingletonHolder {
    private static final Logger LOG = LoggerFactory.getLogger(DependencyDestroyer.class);
    
    private final Object object;
    private final Method preDestroy;
    
    public SingletonHolder(Object object, Method preDestroy) {
        this.object = object;
        this.preDestroy = preDestroy;
    }
    
    public void destroy() throws Exception {
        LOG.info("@PreDestroy : " + getName() + " " + preDestroy.getName());
        this.preDestroy.invoke(object, (Object[])null);
    }

    public String getName() {
        return object.getClass().getCanonicalName();
    }
}
