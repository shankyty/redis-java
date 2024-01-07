package com.github.shankyty.redis;

import java.util.HashMap;
import java.util.Map;

public class ConfigStore implements Store {

    private static final Store INSTANCE = new ConfigStore();

    Map<String, Object> map = new HashMap<>();

    public static Store getInstance() {
        return INSTANCE;
    }

    @Override
    public <V> V get(String key){
        return (V)map.get(key);
    }

    @Override
    public <V> void set(String key, V value) {
        map.put(key, value);
    }

    @Override
    public <V> void set(String key, V value, int expiry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public void cleanup() {
        throw new UnsupportedOperationException();
    }
}
