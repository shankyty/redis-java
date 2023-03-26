package com.github.shankyty.redis;

import java.util.HashMap;

public interface Store {

    <V> V get(String key);
    <V> void set(String key, V value);
}
