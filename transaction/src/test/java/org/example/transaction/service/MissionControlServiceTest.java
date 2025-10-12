package org.example.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.example.transaction.entity.Devil;
import org.example.transaction.repository.DevilRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MissionControlServiceTest {

    private final String testArea = "Tokyo";
    private final List<Devil> testDevils = List.of(new Devil(testArea),
                                                   new Devil(testArea),
                                                   new Devil(testArea));

    @Autowired
    private DevilRepository devilRepository;

    @Autowired
    private MissionControlService missionControlService;

    @Autowired
    private FieldAgentService fieldAgentService;

    @BeforeEach
    void setUp() {
        devilRepository.saveAll(testDevils);
    }

    @Test
    @DisplayName("MySQL InnoDB 엔진에서 Phantom Read가 발생하지 않는다.")
    void phantomReadTest() throws InterruptedException, ExecutionException {
        //given
        CountDownLatch latch = new CountDownLatch(1);

        final ExecutorService controlExecutorService = Executors.newFixedThreadPool(1);
        final ExecutorService agentExecutorService = Executors.newFixedThreadPool(1);

        //when
        final Future<List<Integer>> submit = controlExecutorService.submit(() -> {
            try {
                return missionControlService.findDevilCountFromArea(testArea, latch);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        agentExecutorService.submit(() -> {
            try {
                latch.await();
                fieldAgentService.addDevilInArea(testArea);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        controlExecutorService.shutdown();
        agentExecutorService.shutdown();

        if (!controlExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            controlExecutorService.shutdownNow();
        }
        if (!agentExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            agentExecutorService.shutdownNow();
        }

        //then
        final List<Integer> result = submit.get();
        assertThat(result.get(0)).isEqualTo(testDevils.size());
        assertThat(result.get(1)).isEqualTo(testDevils.size());
    }

    @AfterEach
    void tearDown() {
        devilRepository.deleteAll();
    }
}
