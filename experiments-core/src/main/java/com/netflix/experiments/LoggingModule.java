package com.netflix.experiments;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.google.inject.Inject;
import com.netflix.experiments.status.AppStatus;

public class LoggingModule extends LibraryModule {
    @Inject
    public LoggingModule() {
        super("logging", AppStatus.Up);
        
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile("mylog.log");
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.DEBUG);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
    }
    
    @Override
    protected void configureLibrary() {
        
    }
}
