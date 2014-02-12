package com.netflix.experiments;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * This module is used by {@link Goose} to build a dependency ordered list of 
 * modules in the bootstrap injector
 * 
 * @author elandau
 *
 */
public class DependencyModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(DependencyModule.class);
    
    private final List<Module> modules = Lists.newArrayList();
    
    @Override
    protected void configure() {
        LOG.info("Configuration DependencyModule");
        
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                if (Module.class.isAssignableFrom(type.getRawType())) {
                    encounter.register(new InjectionListener<I>() {
                        @Override
                        public void afterInjection(final I injectee) {
                            LOG.info("Found dependency : " + injectee.getClass().getCanonicalName());
                            modules.add((Module)injectee);
                        }
                    });
                }
            }
        });
    }
    
    public List<Module> getModules() {
        return modules;
    }

}
