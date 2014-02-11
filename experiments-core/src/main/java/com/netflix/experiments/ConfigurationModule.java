package com.netflix.experiments;

import javax.inject.Singleton;

import com.netflix.experiments.status.AppStatus;

@Singleton
public abstract class ConfigurationModule extends LibraryModule {
    public ConfigurationModule(String name) {
        super("configuration", AppStatus.Up);
    }

    @Override
    protected final void configureLibrary() {
        configureConfiguration();
    }
    
    protected abstract void configureConfiguration();
}
