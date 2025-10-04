package org.example.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.example.transaction.entity.DevilHunter;
import org.example.transaction.entity.DevilHunterRank;
import org.example.transaction.repository.DevilHunterRepository;
import org.example.transaction.util.CustomThreadFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DevilHunterProfileServiceTest {

    private DevilHunter devilHunter;

    @Autowired
    private DevilHunterRepository devilHunterRepository;

    @Autowired
    private DevilHunterProfileService devilHunterProfileService;

    @Autowired
    private DevilHunterAdminService devilHunterAdminService;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        devilHunter = devilHunterRepository.saveAndFlush(new DevilHunter("하야카와 아키", DevilHunterRank.Middling));
    }

    @Test
    @DisplayName("아키의 등급 조회 중 정산된 내용이 반영된다. - Read-Commited 격리수준")
    void readCommitedTest() throws InterruptedException, ExecutionException {
        //given

        CountDownLatch latch = new CountDownLatch(1);

        Long testId = devilHunter.getId();
        ExecutorService readExecutorService = Executors.newSingleThreadExecutor(new CustomThreadFactory("분석관-스레드"));
        ExecutorService updateExecutorService = Executors.newSingleThreadExecutor(new CustomThreadFactory("관리자-스레드"));

        //when
        final Future<List<DevilHunterRank>> readFuture = readExecutorService.submit(() -> {
            try {
                return devilHunterProfileService.getHunterRankWithReadCommited(testId, latch);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        updateExecutorService.submit(() -> {
            try {
                latch.await();
                devilHunterAdminService.updateDevilHunterRank(testId, DevilHunterRank.Exceptional);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        readExecutorService.shutdown();
        updateExecutorService.shutdown();

        if (!readExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            readExecutorService.shutdownNow();
        }
        if (!updateExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            updateExecutorService.shutdownNow();
        }

        //then
        final List<DevilHunterRank> result = readFuture.get();
        assertThat(result.get(0)).isEqualTo(DevilHunterRank.Middling);
        assertThat(result.get(1)).isEqualTo(DevilHunterRank.Exceptional);
    }

    @Test
    @DisplayName("아키의 등급 조회 중 정산된 내용이 반영된다. - Repeatable-Read 격리수준")
    void repeatableReadTest() throws InterruptedException, ExecutionException {
        //given

        CountDownLatch latch = new CountDownLatch(1);

        Long testId = devilHunter.getId();
        ExecutorService readExecutorService = Executors.newSingleThreadExecutor(new CustomThreadFactory("분석관-스레드"));
        ExecutorService updateExecutorService = Executors.newSingleThreadExecutor(new CustomThreadFactory("관리자-스레드"));

        //when
        final Future<List<DevilHunterRank>> readFuture = readExecutorService.submit(() -> {
            try {
                return devilHunterProfileService.getHunterRankWithRepeatableRead(testId, latch);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        updateExecutorService.submit(() -> {
            try {
                latch.await();
                devilHunterAdminService.updateDevilHunterRank(testId, DevilHunterRank.Exceptional);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        readExecutorService.shutdown();
        updateExecutorService.shutdown();

        if (!readExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            readExecutorService.shutdownNow();
        }
        if (!updateExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            updateExecutorService.shutdownNow();
        }

        //then
        final List<DevilHunterRank> result = readFuture.get();
        assertThat(result.get(0)).isEqualTo(DevilHunterRank.Middling);
        assertThat(result.get(1)).isEqualTo(DevilHunterRank.Middling);
    }

    @AfterEach
    void tearDown() {
        devilHunterRepository.deleteAll();
    }
}
