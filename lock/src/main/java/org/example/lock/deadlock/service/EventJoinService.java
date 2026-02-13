package org.example.lock.deadlock.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.deadlock.entity.Event;
import org.example.lock.deadlock.entity.EventParticipant;
import org.example.lock.deadlock.entity.Member;
import org.example.lock.deadlock.repository.EventParticipantRepository;
import org.example.lock.deadlock.repository.EventRepository;
import org.example.lock.deadlock.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventJoinService {

    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;

    @Transactional
    public void joinEvent(Long eventId, Long memberId) {
        final Event event = eventRepository.findById(eventId)
                                           .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        final Member member = memberRepository.findById(memberId)
                                              .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventRepository.saveAndFlush(event);

        final EventParticipant participant = EventParticipant.builder()
                                                       .event(event)
                                                       .member(member)
                                                       .build();
        eventParticipantRepository.save(participant);
    }
}
