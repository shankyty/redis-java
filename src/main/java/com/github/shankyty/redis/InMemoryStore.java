package com.github.shankyty.redis;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStore implements Store {

    private static final Store INSTANCE = new InMemoryStore();

    Map<String, Object> map = new HashMap<>();
    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        return (V) map.get(key);
    }

    @Override
    public <V> void set(String key, V value) {
        map.put(key,value);
    }


    public static Store getInstance(){
        return INSTANCE;
    }
}
