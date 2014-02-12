package com.netflix.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.subjects.BehaviorSubject;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.netflix.experiments.status.AppStatus;

public abstract class CommonModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(LibraryModule.class);
    
    private final String name;
    private BehaviorSubject<AppStatus> status;

    public CommonModule(String name) {
        this(name, AppStatus.Starting);
    }
    
    public CommonModule(String name, AppStatus initialStatus) {
        this.name   = name;
        this.status = BehaviorSubject.create(initialStatus);
        
        LOG.info("Creating " + name);
    }
    
    @Override
    protected final void configure() {
        LOG.info("Configuration " + name);
        
        // Add the status binding 
        TypeLiteral<BehaviorSubject<AppStatus>> statusLiteral = new TypeLiteral<BehaviorSubject<AppStatus>>() {};
        MapBinder<String, BehaviorSubject<AppStatus>> statuses = MapBinder.newMapBinder(
                binder(), 
                TypeLiteral.get(String.class),
                statusLiteral);
        
        statuses.addBinding(name).toInstance(status);
        
        bind(statusLiteral).annotatedWith(Names.named(name)).toInstance(status);
        
        // Let library do it's own bindings
        configureModule();
    }
    
    protected abstract void configureModule();

    final void _preConfigure() {
        LOG.info("PreConfiguration " + name);
        preConfigure();
    }
    
    final void _preDestroy() {
        LOG.info("PreDestroy " + name);
        status.onNext(AppStatus.Terminating);
        status.onCompleted();
        preDestroy();
    }
    
    /**
     * Call before guice's configure() from the LifecycleModule
     */
    protected void preConfigure() {
        
    }
    
    protected void preDestroy() {
        
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LibraryModule [name=" + name + "]";
    }
}