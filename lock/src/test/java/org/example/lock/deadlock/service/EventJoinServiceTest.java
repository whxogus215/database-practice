package org.example.lock.deadlock.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.config.IntegrationTest;
import org.example.lock.deadlock.entity.Event;
import org.example.lock.deadlock.entity.Member;
import org.example.lock.deadlock.fixture.ConcurrentTestUtil;
import org.example.lock.deadlock.fixture.TestFixture;
import org.example.lock.deadlock.repository.EventParticipantRepository;
import org.example.lock.deadlock.repository.EventRepository;
import org.example.lock.deadlock.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@IntegrationTest
class EventJoinServiceTest {

    @Autowired
    private EventJoinService eventJoinService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventParticipantRepository participantRepository;

    private Event testEvent;
    private List<Member> testMembers;

    @BeforeEach
    void setUp() {
        testEvent = eventRepository.save(TestFixture.createEvent("테스트 이벤트", 100));
        testMembers = memberRepository.saveAll(TestFixture.createTestMembers(150));
    }

    @Test
    @DisplayName("트랜잭션만으로 동시성 제어가 안 되는 현상을 확인한다.")
    void transactionOnlyTest() throws InterruptedException {
        // when
        ConcurrentTestUtil.executeConcurrentJoins(
            testEvent.getId(),
            testMembers,
            ((eventId, memberId) -> eventJoinService.joinEvent(eventId, memberId))
        );

        // then
        final Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        final long actualParticipantCount = participantRepository.countByEventId(testEvent.getId());

        log.info("=== 트랜잭션 동시성 테스트 결과 ===");
        log.info("이벤트 최대 참가 인원: {}", testEvent.getMaxParticipants());
        log.info("이벤트 현재 참가자 수: {}", updatedEvent.getCurrentParticipants());
        log.info("실제 참가자 테이블 레코드 수: {}", actualParticipantCount);

        assertThat(updatedEvent.getCurrentParticipants()).isNotEqualTo((int) actualParticipantCount);
        assertThat(updatedEvent.getCurrentParticipants()).isLessThanOrEqualTo(testEvent.getMaxParticipants());
    }
}
