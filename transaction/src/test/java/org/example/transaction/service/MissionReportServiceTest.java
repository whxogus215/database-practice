package org.example.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.example.transaction.aop.ConcurrencyTestAspect;
import org.example.transaction.entity.DevilFlesh;
import org.example.transaction.repository.DevilFleshRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class MissionReportServiceTest {

    private final Long INITIAL_PIECES = 10000L;
    private final Long FIND_ID = 1L;

    @Autowired
    private MissionReportService missionReportService;

    @Autowired
    private DevilFleshRepository devilFleshRepository;

    @BeforeEach
    void setUp() {
        devilFleshRepository.saveAndFlush(new DevilFlesh(INITIAL_PIECES));
    }

    @Test
    @DisplayName("마키마의 정산 중 Dirty Read가 발생하여 잘못된 중간 값을 읽는다.")
    void settleMissionTest() throws InterruptedException {
        //given
        long denjiCount = 5L;
        long powerCount = -2L;

        final AtomicLong dirtyReadResult = new AtomicLong();
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        final CountDownLatch updateLatch = new CountDownLatch(1);
        final CountDownLatch readLatch = new CountDownLatch(1);

        ConcurrencyTestAspect.setUpdateLatchs(updateLatch, readLatch);

        //when
        // 스레드 1: 정산 트랜잭션 실행
        executorService.submit(() -> {
            missionReportService.settleMission(denjiCount, powerCount);
        });

        // 스레드 2: 마키마의 현황판 조회 트랜잭션 실행
        executorService.submit(() -> {
            try {
                log.info("마키마 탐색 대기");
                updateLatch.await();

                log.info("마키마 탐색 시작");
                dirtyReadResult.set(missionReportService.findTotalPieces());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                log.info("마키마 스레드는 조회를 마쳤습니다.");
                readLatch.countDown();
            }
        });

        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            log.error("스레드 풀이 5초 내에 종료되지 않았습니다.");
            executorService.shutdownNow();
        }

        //then
        assertThat(dirtyReadResult.get()).isEqualTo(INITIAL_PIECES + denjiCount);

        final DevilFlesh finalResult = devilFleshRepository.findById(FIND_ID).get();
        assertThat(finalResult.getTotalPieces()).isEqualTo(INITIAL_PIECES + denjiCount + powerCount);
    }

    @AfterEach
    void tearDown() {
        devilFleshRepository.deleteAll();
    }
}
