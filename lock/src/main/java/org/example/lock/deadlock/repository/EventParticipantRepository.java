package org.example.lock.deadlock.repository;

import org.example.lock.deadlock.entity.Event;
import org.example.lock.deadlock.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
}
