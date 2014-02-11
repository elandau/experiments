package com.netflix.experiments;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import rx.subjects.BehaviorSubject;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.netflix.experiments.status.AppStatus;
import com.netflix.experiments.status.AppStatusProvider;
import com.netflix.governator.annotations.binding.Background;

@Singleton
public class PlatformModule extends LibraryModule {
    @Inject
    public PlatformModule(LoggingModule logging) {
        super("platform", AppStatus.Up);
    }

    @Override
    protected void configureLibrary() {
        TypeLiteral<BehaviorSubject<AppStatus>> statusLiteral = new TypeLiteral<BehaviorSubject<AppStatus>>() {};
        bind(statusLiteral).toProvider(AppStatusProvider.class);
        
        bind(ScheduledExecutorService.class)
            .annotatedWith(Background.class)
            .toInstance(
                Executors.newScheduledThreadPool(
                    10, 
                    new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("Background-%d")
                        .build()
                        ));
    }

}
