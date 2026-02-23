package org.example.lock.deadlock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events_with_lock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class EventWithLock {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "events_id")
    private Long id;

    private String name;
    private String description;
    private LocalDateTime eventDate;
    private int maxParticipants;
    private int currentParticipants;

    @Builder
    public EventWithLock(String name, String description, LocalDateTime eventDate, int maxParticipants) {
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 0;
    }

    public void increaseParticipants() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("최대 참가 인원을 초과했습니다.");
        }
        this.currentParticipants++;
    }
}
