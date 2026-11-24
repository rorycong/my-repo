package splitter.server.controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages fine-grained per-group locks so changes to different groups do not block each other.
 */
public class GroupLockManager {
    private final ConcurrentMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public GroupLockManager() {}

    public void lock(long groupId) {
        ReentrantLock lock = locks.computeIfAbsent(groupId, k -> new ReentrantLock());
        lock.lock();
    }

    public void unlock(long groupId) {
        ReentrantLock lock = locks.get(groupId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
