/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.decompile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简单的 LRU 缓存，用于缓存反编译结果
 */
public class LRUCache {
    private static final int DEFAULT_MAX_SIZE = 128;
    private final Map<String, String> cache;

    public LRUCache() {
        this(DEFAULT_MAX_SIZE);
    }

    public LRUCache(int maxSize) {
        this.cache = new LinkedHashMap<String, String>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > maxSize;
            }
        };
    }

    public String get(String key) {
        return cache.get(key);
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }
}
