package com.netflix.experiments;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.netflix.experiments.lifecycle.PostConstructTask;
import com.netflix.experiments.lifecycle.SingletonHolder;
import com.netflix.governator.annotations.WarmUp;
import com.netflix.governator.configuration.ConfigurationProvider;

@Singleton
public class LifecycleModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(LifecycleModule.class);

    private final CopyOnWriteArraySet<SingletonHolder> singletons = new CopyOnWriteArraySet<SingletonHolder>();
    private final ExecutorService warmupExecutor;
    private final ConfigurationProvider config;
    
    @Inject
    public LifecycleModule(ConfigurationModule config) {
        this.config = config.getConfigurationProvider();
        this.warmupExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("WarmUp-%d").build());
    }
    
    @Override
    protected void configure() {
        bind(new TypeLiteral<Set<SingletonHolder>>() {}).toInstance(singletons);
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, final TypeEncounter<I> encounter) {
                encounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(final I injectee) {
                        LOG.info("Creating instance : " + injectee.getClass().getCanonicalName());
                        
                        Method postConstruct = null;
                        Method preDestroy    = null;
                        for (Method method : injectee.getClass().getMethods()) {
                            if (postConstruct == null) {
                                PostConstruct annot = method.getAnnotation(PostConstruct.class);
                                if (annot != null) {
                                    postConstruct = method;
                                }
                            }
                            
                            if (preDestroy == null) {
                                PreDestroy annot = method.getAnnotation(PreDestroy.class);
                                if (annot != null) {
                                    preDestroy = method;
                                }
                            }
                        }
                        
                        boolean isSingleton = injectee.getClass().getAnnotation(Singleton.class) != null;
                        if (!isSingleton) {
                            isSingleton = injectee.getClass().getAnnotation(javax.inject.Singleton.class) != null;
                        }
                        if (postConstruct != null) {
                            try {
                                if (postConstruct.getAnnotation(WarmUp.class) != null) {
                                    warmupExecutor.submit(new PostConstructTask(injectee, postConstruct));
                                }
                                else {
                                    new PostConstructTask(injectee, postConstruct).run();
                                }
                            } catch (Exception e) {
                                Exception error = new Exception("Error calling @PostConstruct method " + injectee.getClass().getCanonicalName() + " " + postConstruct.getName(), e);
                                encounter.addError(e);
                                LOG.error(error.getMessage(), e);
                            }
                        }
                        
                        if (preDestroy != null) {
                            if (isSingleton) {
                                singletons.add(new SingletonHolder(injectee, preDestroy));
                            }
                            else {
                                LOG.error("@PreDestroy on " + injectee.getClass().getCanonicalName() + " will not be called because it is not a Singleton");
                            }
                        }
                        
                        if (ScheduledExecutorService.class.isAssignableFrom(injectee.getClass())) {
                            try {
                                singletons.add(new SingletonHolder(injectee, injectee.getClass().getMethod("shutdown")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }
}
