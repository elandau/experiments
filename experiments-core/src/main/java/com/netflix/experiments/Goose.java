package com.netflix.experiments;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;

public class Goose {
    public static Injector createInjector(Class<? extends ApplicationModule> appModule) {
        // Resolve all module dependencies and generate a list of modules to 
        // add to the injector.  These modules will be in order of dependency.
        final DependencyModule dependencies = new DependencyModule();
        Injector bootstrapInjector = Guice.createInjector(dependencies);
        bootstrapInjector.getInstance(appModule);
        
        final List<Module> modules = Lists.newArrayList();
        modules.addAll(dependencies.getModules());
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                // We will need this for the destroyer
                bind(new TypeLiteral<List<LibraryModule>>() {}).toInstance(dependencies.getModules());
                bind(DepdencyDestroyer.class).asEagerSingleton();
            }
        });
        modules.add(new LifecycleModule());
        
        // Call preConfigure() before any of the libraries guice configure() is called
        // preConfigure is called in order of dependency
        for (LibraryModule library : dependencies.getModules()) {
            library._preConfigure();
        }

        // Finally, create the one and only injector
        return Guice.createInjector(Stage.PRODUCTION, modules);
    }
}
