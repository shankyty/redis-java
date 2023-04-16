package com.github.shankyty.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.now;

public class InMemoryStore implements Store {

    private static final Store INSTANCE = new InMemoryStore();

    Map<String, Object> map = new HashMap<>();

    Map<String, Long> expiry = new HashMap<>();
    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        V val = (V)map.get(key);
        Long  time = expiry.get(key);
        if(time != null  ){
            long now = Instant.now().toEpochMilli();
            if(time <= now){
                map.remove(key);
                expiry.remove(key);
                return null;
            }
        }

        return val;
    }

    @Override
    public <V> void set(String key, V value) {
        map.put(key,value);
    }

    @Override
    public <V> void set(String key, V value, int expiry) {
        this.map.put(key, value);
        this.expiry.put(key, now().plus(Duration.ofMillis(expiry)).toEpochMilli());
    }


    public static Store getInstance(){
        return INSTANCE;
    }
}
