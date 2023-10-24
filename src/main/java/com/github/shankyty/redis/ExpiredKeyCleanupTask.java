package com.github.shankyty.redis;

public class ExpiredKeyCleanupTask  implements HashWheelTimer.Task {

    private String key;
    private long expiry;

    private Store store;

    public ExpiredKeyCleanupTask(String key,
                                 long expiry,
                                 Store store) {
        this.key = key;
        this.expiry = expiry;
        this.store = store;
    }


    public long getExpiry() {
        return expiry;
    }

    public void execute() {
        store.remove(key);
    }

    @Override
    public String toString() {
        return "ExpiredKeyCleanupTask{" +
                "key='" + key + '\'' +
                ", expiry=" + expiry +
                '}';
    }
}