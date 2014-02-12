package com.netflix.experiments;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.configuration.SystemConfigurationProvider;

@Singleton
public class SystemConfigurationModule extends ConfigurationModule {

    public SystemConfigurationProvider config;

    @Inject
    public SystemConfigurationModule() {
        super("system_configuration");
        
        config = new SystemConfigurationProvider();
    }

    @Override
    protected void configureConfiguration() {
        bind(ConfigurationProvider.class).toInstance(config);
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return config;
    }

}
