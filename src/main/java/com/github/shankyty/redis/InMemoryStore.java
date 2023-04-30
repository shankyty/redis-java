package com.github.shankyty.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.now;

public class InMemoryStore implements Store {

    private static final Store INSTANCE = new InMemoryStore();

    Map<String, Object> map = new HashMap<>();

    Map<String, ExpiredKeyCleanupTask> expiry = new HashMap<>();

    HashWheelTimer  timer = new HashWheelTimer(1000, 1000);

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        V val = (V)map.get(key);
        ExpiredKeyCleanupTask  task = expiry.get(key);
        if(task != null  ){
            long now = Instant.now().toEpochMilli();
            if(task.getExpiry() <= now){
                task.execute();
                return null;
            }
        }

        return val;
    }

    @Override
    public <V> void set(String key,
                        V value) {
        map.put(key,value);
    }

    @Override
    public <V> void set(String key,
                        V value,
                        int expiry) {
        this.map.put(key, value);
        ExpiredKeyCleanupTask cleanupTask = new ExpiredKeyCleanupTask(key,
                now().plus(Duration.ofMillis(expiry)).toEpochMilli(),
                this);
        this.expiry.put(key,cleanupTask);
        timer.addTimer(cleanupTask.getExpiry(),
                cleanupTask);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
        expiry.remove(key);
    }


    @Override
    public void cleanup() {
       timer.tick();
    }


    public static Store getInstance(){
        return INSTANCE;
    }
}
