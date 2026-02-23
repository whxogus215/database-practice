package org.example.lock.db_lock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.config.IntegrationTest;
import org.example.lock.deadlock.entity.EventWithLock;
import org.example.lock.deadlock.entity.Member;
import org.example.lock.deadlock.facade.OptimisticLockEventFacade;
import org.example.lock.deadlock.repository.EventParticipantWithLockRepository;
import org.example.lock.deadlock.repository.EventWithLockRepository;
import org.example.lock.deadlock.repository.MemberRepository;
import org.example.lock.deadlock.service.EventJoinWithLockService;
import org.example.lock.fixture.ConcurrentTestUtil;
import org.example.lock.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@IntegrationTest
class EventJoinServiceTest {

    @Autowired
    private OptimisticLockEventFacade optimisticLockEventFacade;

    @Autowired
    private EventJoinWithLockService eventJoinWithLockService;

    @Autowired
    private EventWithLockRepository eventWithLockRepository;

    @Autowired
    private EventParticipantWithLockRepository eventParticipantRepository;

    @Autowired
    private MemberRepository memberRepository;

    private EventWithLock testEvent;
    private List<Member> testMembers;

    private static final int THREAD_COUNT = 150;
    private static final int MAX_PARTICIPANT = 100;

    @BeforeEach
    void setUp() {
        testEvent = eventWithLockRepository.saveAndFlush(
            TestFixture.createEventWithLock("테스트 이벤트", MAX_PARTICIPANT)
        );
        testMembers = memberRepository.saveAll(
            TestFixture.createTestMembers(THREAD_COUNT)
        );
    }

    @AfterEach
    void cleanUp() {
        eventParticipantRepository.deleteAll();
        eventWithLockRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락으로 150명 동시 참가 테스트")
    void pessimisticLockTest() throws InterruptedException {
        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentJoins(
            testEvent.getId(),
            testMembers,
            (eventId, memberId) -> eventJoinWithLockService.joinEventWithPessimisticLock(eventId, memberId)
        );
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        EventWithLock event = eventWithLockRepository.findById(testEvent.getId()).orElseThrow();
        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("최종 참가자 수: {}", event.getCurrentParticipants());

        assertThat(event.getCurrentParticipants()).isEqualTo(MAX_PARTICIPANT);
    }

    @Test
    @DisplayName("낙관적 락으로 150명 동시 참가 테스트")
    void optimisticLockTest() throws InterruptedException {
        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentJoins(
            testEvent.getId(),
            testMembers,
            (eventId, memberId) -> optimisticLockEventFacade.participateWithOptimisticLock(eventId, memberId)
        );
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        EventWithLock event = eventWithLockRepository.findById(testEvent.getId()).orElseThrow();
        log.info("=== 낙관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("최종 참가자 수: {}", event.getCurrentParticipants());

        assertThat(event.getCurrentParticipants()).isEqualTo(MAX_PARTICIPANT);
    }
}
