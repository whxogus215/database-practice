package org.example.lock.fixture;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.deadlock.entity.Member;

@Slf4j
public class ConcurrentTestUtil {

    @FunctionalInterface
    public interface EventJoinTask {

        void join(Long eventId, Long memberId) throws Exception;
    }

    public static void executeConcurrentJoins(
        Long eventId,
        List<Member> members,
        EventJoinTask joinTask
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(members.size());

        for (Member member : members) {
            executorService.submit(() -> {
                try {
                    joinTask.join(eventId, member.getId());
                } catch (Exception e) {
                    log.error("이벤트 참가 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
    }
}
