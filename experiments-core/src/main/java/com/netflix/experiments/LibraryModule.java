package com.netflix.experiments;

import com.netflix.experiments.status.AppStatus;

/**
 * Base class for any library in the framework
 * 
 * Exposed bindings
 * 
 *  @Named("libraryname") BehaviorSubject<AppStatus> 
 *  
 *  Map<String, BehaviorSubject<AppStatus>> 
 * 
 * @author elandau
 *
 */
public abstract class LibraryModule extends CommonModule {
    
    public LibraryModule(String name, AppStatus initialStatus) {
        super(name, initialStatus);
    }

    public LibraryModule(String name) {
        super(name);
    }
    
    public final void configureModule() {
        configureLibrary();
    }
    protected abstract void configureLibrary();
    
}
