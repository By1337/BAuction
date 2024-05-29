package org.by1337.bauction.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class ThreadCreator {
    public static ThreadFactoryBuilder createBuilder(){
        return new ThreadFactoryBuilder();
    }
    public static ThreadFactory createWithName(String nameFormat){
        return createBuilder().setNameFormat(nameFormat).build();
    }
    public static Thread createThreadWithName(String nameFormat, Runnable runnable){
        return createWithName(nameFormat).newThread(runnable);
    }
}
