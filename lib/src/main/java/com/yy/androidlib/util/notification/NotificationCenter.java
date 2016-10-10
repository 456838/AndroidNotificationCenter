package com.yy.androidlib.util.notification;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOS NSNotificationCenter for Android
 */
public enum NotificationCenter {
    INSTANCE;

    public static final String TAG = "NotificationCenter";
    private Map<Class<?>, Notification> notificationMap;
    private long mainThreadId;
    private Handler handler;
    private Map<Object, Boolean> allObserver;

    NotificationCenter() {
        notificationMap = new ConcurrentHashMap<>();
        Looper mainLooper = Looper.getMainLooper();
        handler = new Handler(mainLooper);
        mainThreadId = mainLooper.getThread().getId();
        allObserver = new ConcurrentHashMap<>();
    }

    /**
     * add observer by annotation
     *
     * @param observer
     */
    public void addObserver(final Object observer) {
        if (isMainThread()) {
            doAddObserver(observer);
        } else {
            Log.w(TAG, String.format("trying to add observer in non main thread: " + observer.getClass()));
            handler.post(new Runnable() {
                public void run() {
                    doAddObserver(observer);
                }
            });
        }
    }

    private boolean isMainThread() {
        return Thread.currentThread().getId() == mainThreadId;
    }

    private void doAddObserver(Object observer) {
        allObserver.put(observer, true);

        for (Iterator<Class<?>> it = notificationMap.keySet().iterator(); it.hasNext(); ) {
            Class<?> callback = it.next();
            Notification notification = notificationMap.get(callback);
            if (callback.isInstance(observer)) {
                notification.getObservers().put(observer, true);
            }
        }
    }

    public void removeObserver(final Object observer) {
        if (isMainThread()) {
            doRemoveObserver(observer);
        } else {
            Log.w(TAG, String.format("trying to remove observer in non main thread: " + observer.getClass()));
            removeObserverLater(observer);
        }
    }

    /**
     * observer will be removed later
     *
     * @param observer
     */
    private void removeObserverLater(final Object observer) {
        handler.post(new Runnable() {
            public void run() {
                doRemoveObserver(observer);
            }
        });
    }

    private void doRemoveObserver(Object observer) {
        allObserver.remove(observer);

        for (Iterator<Class<?>> it = notificationMap.keySet().iterator(); it.hasNext(); ) {
            Class<?> callback = it.next();
            Notification notification = notificationMap.get(callback);
            if (callback.isInstance(observer)) {
                notification.getObservers().remove(observer);
            }
        }
    }

    /**
     * @param callback
     * @return not null
     */
    private <T> Notification<T> getNotification(Class<T> callback) {
        Notification notification = notificationMap.get(callback);
        if (notification == null) {
            notification = addNotification(callback);
        }
        return notification;
    }

    private <T> Notification<T> addNotification(Class<T> callback) {
        Notification<T> notification = notificationMap.get(callback);
        if (notification == null) {
            notification = new Notification<T>(callback, handler);
            notificationMap.put(callback, notification);
        }

        //addobserver
        for (Object observer : allObserver.keySet()) {
            if (callback.isInstance(observer)) {
                notification.getObservers().put(observer, true);
            }
        }
        return notification;
    }

    public <T> T getObserver(Class<T> callback) {
        return getNotification(callback).getObserver();
    }

    public void removeAll() {
        notificationMap.clear();
    }

    @Deprecated
    /**
     * @deprecated no need to add callback before invoking since release 1.0.31
     */
    public void addCallbacks(Class callbackParent) {
        // do nothing
    }
}
