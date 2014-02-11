package com.netflix.experiments;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.subjects.BehaviorSubject;
import rx.util.functions.Action1;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.experiments.status.AppStatus;
import com.netflix.governator.annotations.WarmUp;
import com.netflix.governator.annotations.binding.Background;

public class AppStatusTest {
    private static final Logger LOG = LoggerFactory.getLogger(AppStatusTest.class);
    
    @Singleton
    public static class LibraryA {
        final BehaviorSubject<AppStatus> libStatus;
        
        @Inject
        public LibraryA(
                final @Named("liba") BehaviorSubject<AppStatus> libStatus,
                @Background ScheduledExecutorService backgroundService) {
            this.libStatus = libStatus;
//            backgroundService.scheduleAtFixedRate(new Runnable() {
//                private AppStatus status = AppStatus.Starting;
//                
//                @Override
//                public void run() {
//                    libStatus.onNext(status);
//                    if (status != AppStatus.Up) 
//                        status = AppStatus.Up;
//                    else 
//                        status = AppStatus.Down;
//                }
//            }, 1, 1, TimeUnit.SECONDS);
        }
        
        @PostConstruct 
        @WarmUp
        public void init() throws InterruptedException {
            TimeUnit.SECONDS.sleep(3);
            libStatus.onNext(AppStatus.Up);
        }
        
        @PreDestroy
        public void shutdown() {
            
        }
    }
    
    @Singleton
    public static class LibraryAModule extends LibraryModule {
        @Inject
        public LibraryAModule(LibraryBModule libraryB, PlatformModule platform) {
            super("liba");
        }

        @Override
        protected void configureLibrary() {
            bind(LibraryA.class).asEagerSingleton();
        }
    }
    
    /// A -> B -> C
    /// A -> D -> C
    @Singleton
    public static class LibraryB {
        @Inject
        public LibraryB(
                final @Named("libb") BehaviorSubject<AppStatus> libStatus,
                @Background ScheduledExecutorService backgroundService) {
          libStatus.onNext(AppStatus.Up);
            
//            backgroundService.scheduleAtFixedRate(new Runnable() {
//                private AppStatus status = AppStatus.Starting;
//                
//                @Override
//                public void run() {
//                    libStatus.onNext(status);
//                    if (status != AppStatus.Up) 
//                        status = AppStatus.Up;
//                    else 
//                        status = AppStatus.Down;
//                }
//            }, 3, 3, TimeUnit.SECONDS);
        }
        
        @PostConstruct 
        @WarmUp
        public void init() {
            
        }
        
        @PreDestroy
        public void shutdown() {
            
        }

    }
    
    @Singleton
    public static class LibraryBModule extends LibraryModule {
        @Inject
        public LibraryBModule(PlatformModule platform) {
            super("libb");
        }

        @Override
        protected void configureLibrary() {
            bind(LibraryB.class).asEagerSingleton();
        }
    }
    
    @Singleton
    public static class ApplicationFoo extends ApplicationModule {

        @Inject
        public ApplicationFoo(LibraryAModule module, PlatformModule platform) {
            super("applicationFoo");
        }
        
        @Override
        protected void configureApplication() {
            bind(ServiceFoo.class).asEagerSingleton();
        }

    }
    
    @Singleton
    public static class ServiceFoo {
        private ScheduledExecutorService backgroundService;
        
        @Inject
        public ServiceFoo(
                BehaviorSubject<AppStatus> appStatus,
                @Background ScheduledExecutorService backgroundService
                ) {
            appStatus.subscribe(new Action1<AppStatus>() {
                @Override
                public void call(AppStatus t1) {
                    LOG.info("App is " + t1);
                }
            });
        }
        
        @PostConstruct 
        public void init() {
        }
        
        @PreDestroy
        public void shutdown() {
            
        }
    }
    
    @Test
    public void statusTest() throws Exception {
        Injector injector = Goose.createInjector(ApplicationFoo.class);
        
        try {
            TimeUnit.SECONDS.sleep(10);
        }
        finally {
            DepdencyDestroyer destroyer = injector.getInstance(DepdencyDestroyer.class);
            destroyer.destroy();
        }
    }
}
