package com.netflix.experiments;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.netflix.experiments.lifecycle.SingletonHolder;

/**
 * 'Utility' class used to invoke all the libraries' preDestroy() methods in 
 * reverse order to the dependencies
 * 
 * @author elandau
 *
 */
@Singleton
public class DepdencyDestroyer {
    private static final Logger LOG = LoggerFactory.getLogger(DepdencyDestroyer.class);
    
    private final List<LibraryModule> libraries;
    private final Set<SingletonHolder> singletons;
    
    @Inject
    public DepdencyDestroyer(List<LibraryModule> libraries, Set<SingletonHolder> singletons) {
        this.libraries = libraries;
        this.singletons = singletons;
    }
    
    public synchronized void destroy() {
        LOG.info("Destroying singletons");
        for (SingletonHolder singleton : singletons) {
            try {
                singleton.destroy();
            } catch (Exception e) {
                LOG.error("Failed to destroy singleton : " + singleton.getName(), e);
            }
        }
        
        LOG.info("Destroying libraries");
        for (LibraryModule library : Lists.reverse(libraries)) {
            library._preDestroy();
        }
    }
}
