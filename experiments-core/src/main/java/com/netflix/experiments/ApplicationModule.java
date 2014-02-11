package com.netflix.experiments;

import com.google.inject.name.Names;
import com.netflix.experiments.status.AppStatus;

/**
 * Any application should implement this module.  The concrete application class is the
 * starting point for building the library dependency graph.  The application should
 * have an @Inject constructor with dependencies on any of it's dependent libraries.
 * 
 * @author elandau
 *
 */
public abstract class ApplicationModule extends LibraryModule {
    public ApplicationModule(String applicationName) {
        super(applicationName, AppStatus.Up);
    }
    
    @Override
    final protected void configureLibrary() {
        bind(String.class).annotatedWith(Names.named("appName")).toInstance(getName());
        configureApplication();
    }
    
    protected abstract void configureApplication();
    
    @Override
    public String toString() {
        return "ApplicationModule [name=" + getName() + "]";
    }
}
