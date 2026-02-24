package org.example.lock.deadlock.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.example.lock.deadlock.entity.EventWithLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventWithLockRepository extends JpaRepository<EventWithLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventWithLock e where e.id = :id")
    Optional<EventWithLock> findByIdWithPessimisticLock(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select e from EventWithLock e where e.id = :id")
    Optional<EventWithLock> findByIdWithOptimisticLock(Long id);

    @Query(value = "SELECT GET_LOCK(:key, :timeoutSeconds)", nativeQuery = true)
    Integer getLock(String key, int timeoutSeconds);

    @Query(value = "SELECT RELEASE_LOCK(:key)", nativeQuery = true)
    Integer releaseLock(String key);
}
