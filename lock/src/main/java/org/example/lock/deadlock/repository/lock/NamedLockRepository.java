package org.example.lock.deadlock.repository.lock;

public interface NamedLockRepository {

    Integer getLock(String key, int timeoutSeconds);

    Integer releaseLock(String key);
}
