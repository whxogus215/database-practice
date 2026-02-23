package org.example.lock.deadlock.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.deadlock.entity.EventParticipantWithLock;
import org.example.lock.deadlock.entity.EventWithLock;
import org.example.lock.deadlock.entity.Member;
import org.example.lock.deadlock.repository.EventParticipantWithLockRepository;
import org.example.lock.deadlock.repository.EventWithLockRepository;
import org.example.lock.deadlock.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventJoinWithLockService {

    private final MemberRepository memberRepository;
    private final EventWithLockRepository eventRepository;
    private final EventParticipantWithLockRepository eventParticipantRepository;

    @Transactional
    public void joinEventWithPessimisticLock(Long eventId, Long memberId) {
        final EventWithLock event = eventRepository.findByIdWithPessimisticLock(eventId)
                                                   .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        final Member member = memberRepository.findById(memberId)
                                              .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventRepository.save(event);

        final EventParticipantWithLock participant = EventParticipantWithLock.builder()
                                                                             .event(event)
                                                                             .member(member)
                                                                             .build();
        eventParticipantRepository.save(participant);
    }
}
