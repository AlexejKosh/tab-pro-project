package com.alexey.tabgenerator.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GenerationLockService {

    private final Set<String> activeIps = ConcurrentHashMap.newKeySet();

    public boolean tryLock(String ip) {
        return activeIps.add(ip);
    }

    public void unlock(String ip) {
        activeIps.remove(ip);
    }
}
