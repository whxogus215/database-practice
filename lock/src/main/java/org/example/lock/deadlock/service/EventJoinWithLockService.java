package org.example.lock.deadlock.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.deadlock.entity.EventParticipantWithLock;
import org.example.lock.deadlock.entity.EventWithLock;
import org.example.lock.deadlock.entity.Member;
import org.example.lock.deadlock.repository.EventParticipantWithLockRepository;
import org.example.lock.deadlock.repository.EventWithLockRepository;
import org.example.lock.deadlock.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void joinEventWithOptimisticLock(Long eventId, Long memberId) {
        final EventWithLock event = eventRepository.findByIdWithOptimisticLock(eventId)
                                                   .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        final Member member = memberRepository.findById(memberId)
                                              .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventRepository.saveAndFlush(event); // 낙관적 락에서의 데드락 발생을 막기 위한 saveAndFlush() 호출

        final EventParticipantWithLock participant = EventParticipantWithLock.builder()
                                                                             .event(event)
                                                                             .member(member)
                                                                             .build();
        eventParticipantRepository.save(participant);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void joinEventWithNamedLock(Long eventId, Long memberId) {
        final EventWithLock event = eventRepository.findByIdWithOptimisticLock(eventId)
                                                   .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        final Member member = memberRepository.findById(memberId)
                                              .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventRepository.saveAndFlush(event);

        final EventParticipantWithLock participant = EventParticipantWithLock.builder()
                                                                             .event(event)
                                                                             .member(member)
                                                                             .build();
        eventParticipantRepository.save(participant);
    }
}
