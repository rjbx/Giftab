package com.github.rjbx.givetrack;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// TODO Synchronize data API requests with FirebaseAuth and FirebaseDatabase operations through network thread

/**
 * Exposes main and worker threads for synchronizing code execution.
 */
public final class AppExecutors {

    private static final Object LOCK = new Object();
    private static AppExecutors sInstance;
    private final Executor networkIO;
    private final Executor diskIO;
    private final Executor mainThread;

    private AppExecutors(Executor networkIO, Executor diskIO, Executor mainThread) {
        this.networkIO = networkIO;
        this.diskIO = diskIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new AppExecutors(
                            Executors.newFixedThreadPool(3),
                            Executors.newSingleThreadExecutor(),
                            new MainThreadExecutor()
                    );
                }
            }
        }
        return sInstance;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) { mainThreadHandler.post(command); }
    }
}