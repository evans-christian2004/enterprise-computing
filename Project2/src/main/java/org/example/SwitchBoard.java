package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
/*
Name: Christian Evans
Course: CNT 4714 Spring 2026
Assignment title: Project 2 – Multi-threaded programming in Java
Date: February 15, 2026
Class: Enterprise Computing
Description: Manages all switch locks in the yard.
*/

public class SwitchBoard {
    private final Map<Integer, ReentrantLock> switchLocks = new ConcurrentHashMap<>();

    public SwitchBoard(int maxSwitchId) {
        for (int i = 1; i <= maxSwitchId; i++) {
            switchLocks.put(i, new ReentrantLock());
        }
    }

    public boolean tryLock(int switchId) {
        ReentrantLock lock = switchLocks.get(switchId);
        if (lock == null) {
            return false;
        }
        return lock.tryLock();
    }

    public void unlock(int switchId) {
        ReentrantLock lock = switchLocks.get(switchId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
