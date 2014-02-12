package com.netflix.experiments;

import com.google.inject.ImplementedBy;
import com.netflix.experiments.status.AppStatus;
import com.netflix.governator.configuration.ConfigurationProvider;

@ImplementedBy(SystemConfigurationModule.class)
public abstract class ConfigurationModule extends LibraryModule {
    public ConfigurationModule(String name) {
        super("configuration", AppStatus.Up);
    }

    @Override
    protected final void configureLibrary() {
        configureConfiguration();
    }
    
    public abstract ConfigurationProvider getConfigurationProvider();
    
    protected abstract void configureConfiguration();
}
