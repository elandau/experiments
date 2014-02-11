package com.netflix.experiments.status;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.util.functions.Action1;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Singleton
public class AppStatusProvider implements Provider<BehaviorSubject<AppStatus>>{
    private static final Logger LOG = LoggerFactory.getLogger(AppStatusProvider.class);
    
    private final Map<String, BehaviorSubject<AppStatus>> statuses;
    private final Subscription sub;
    private final BehaviorSubject<AppStatus> subject;
    private int upCount = 0;
    
    @Inject
    public AppStatusProvider(Map<String, BehaviorSubject<AppStatus>> statuses) {
        LOG.info("Started");
        
        this.statuses = statuses;
        this.subject = BehaviorSubject.create(AppStatus.Starting);
        this.sub = Observable.from(Lists.newArrayList(statuses.entrySet()))
            .subscribe(new Action1<Map.Entry<String, BehaviorSubject<AppStatus>>>() {
                @Override
                public void call(final Entry<String, BehaviorSubject<AppStatus>> library) {
                    LOG.info("Library : " + library.getKey());
                    library.getValue().subscribe(new Action1<AppStatus>() {
                        private AppStatus currentStatus = AppStatus.Starting;
                        
                        @Override
                        public synchronized void call(AppStatus status) {
                            if (currentStatus == status)
                                return;
                            
                            LOG.info(library.getKey() + " " + currentStatus + " -> " + status);
                            
                            try {
                                if (status == AppStatus.Up) {
                                    incrementUpStatus();
                                }
                                else {
                                    decrementUpStatus();
                                }
                                    
                            }
                            finally {
                                currentStatus = status;
                            }
                        }
                        
                    });
                }
            });
    }
    
    @Override
    public BehaviorSubject<AppStatus> get() {
        return subject;
    }
    
    public synchronized void incrementUpStatus() {
        upCount++;
        if (upCount == statuses.size()) {
            subject.onNext(AppStatus.Up);
        }
    }
    
    public synchronized void decrementUpStatus() {
        upCount--;
        if (upCount == statuses.size() - 1) {
            subject.onNext(AppStatus.Down);
        }
    }

}
