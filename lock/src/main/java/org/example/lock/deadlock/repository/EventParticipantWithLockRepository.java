package org.example.lock.deadlock.repository;

import org.example.lock.deadlock.entity.EventParticipantWithLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventParticipantWithLockRepository extends JpaRepository<EventParticipantWithLock, Long> {

}
