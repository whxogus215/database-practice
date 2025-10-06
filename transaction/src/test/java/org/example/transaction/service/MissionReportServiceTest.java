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
import org.example.transaction.util.CustomThreadFactory;
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

        final ExecutorService settlementExecutor = Executors.newSingleThreadExecutor(new CustomThreadFactory("정산-스레드"));
        final ExecutorService dashboardExecutor = Executors.newSingleThreadExecutor(new CustomThreadFactory("마키마-스레드"));

        final CountDownLatch updateLatch = new CountDownLatch(1);
        final CountDownLatch readLatch = new CountDownLatch(1);

        ConcurrencyTestAspect.setUpdateLatchs(updateLatch, readLatch);

        //when
        // 스레드 1: 정산 트랜잭션 실행
        settlementExecutor.submit(() -> {
            missionReportService.settleMission(denjiCount, powerCount);
        });

        // 스레드 2: 마키마의 현황판 조회 트랜잭션 실행
        dashboardExecutor.submit(() -> {
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

        settlementExecutor.shutdown();
        dashboardExecutor.shutdown();

        if (!settlementExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            settlementExecutor.shutdownNow();
        }
        if (!dashboardExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            dashboardExecutor.shutdownNow();
        }

        //then

        // Dirty Read로 인해, 마키마는 덴지의 살점만 추가된 "커밋되지 않은" 데이터를 조회한다.
        // 만약, 정산 트랜잭션이 "롤백"될 경우, 마키마는 총의 살점 개수를 잘못 알고있게 된다.
        assertThat(dirtyReadResult.get()).isEqualTo(INITIAL_PIECES + denjiCount);

        final DevilFlesh finalResult = devilFleshRepository.findById(FIND_ID).get();
        assertThat(finalResult.getTotalPieces()).isEqualTo(INITIAL_PIECES + denjiCount + powerCount);
    }

    @AfterEach
    void tearDown() {
        devilFleshRepository.deleteAll();
    }
}
