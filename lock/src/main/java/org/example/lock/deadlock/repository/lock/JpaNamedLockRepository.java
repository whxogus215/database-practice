package org.example.lock.deadlock.repository.lock;

import org.example.lock.deadlock.entity.EventWithLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNamedLockRepository extends JpaRepository<EventWithLock, Long>, NamedLockRepository {

    @Query(value = "SELECT GET_LOCK(:key, :timeoutSeconds)", nativeQuery = true)
    Integer getLock(String key, int timeoutSeconds);

    @Query(value = "SELECT RELEASE_LOCK(:key)", nativeQuery = true)
    Integer releaseLock(String key);
}
