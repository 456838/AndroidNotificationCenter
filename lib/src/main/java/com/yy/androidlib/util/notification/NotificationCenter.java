package com.yy.androidlib.util.notification;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOS NSNotificationCenter for Android
 */
public enum NotificationCenter {
    INSTANCE;

    public static final String TAG = "notification";
    private Map<Class<?>, Notification> notificationMap;
    private long mainThreadId;
    private Handler handler;

    NotificationCenter() {
        notificationMap = new HashMap<>();
        Looper mainLooper = Looper.getMainLooper();
        handler = new Handler(mainLooper);
        mainThreadId = mainLooper.getThread().getId();
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
        Class<?> inters[] = getInterfaces(observer);
        for (int i = 0; i < inters.length; i++) {
            Notification notification = getNotification(inters[i]);
            notification.getObservers().put(observer, true);
        }
    }

    private Class<?> [] getInterfaces(Object observer) {
        Class<?> clazz = observer.getClass();
        Class<?> inters[] = clazz.getInterfaces();
        return inters;
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
        Class<?> inters[] = getInterfaces(observer);
        for (int i = 0; i < inters.length; i++) {
            Notification notification = getNotification(inters[i]);
            notification.getObservers().remove(observer);
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
