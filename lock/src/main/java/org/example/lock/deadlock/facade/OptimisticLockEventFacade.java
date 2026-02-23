package org.example.lock.deadlock.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.deadlock.service.EventJoinWithLockService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockEventFacade {

    private static final long RETRY_DELAY = 50;

    private final EventJoinWithLockService eventJoinWithLockService;

    public void participateWithOptimisticLock(Long eventId, Long memberId) throws InterruptedException {
        int tryCount = 1;

        while (true) {
            try {
                eventJoinWithLockService.joinEventWithOptimisticLock(eventId, memberId);
                log.info("이벤트 참가 성공 - eventId: {}, memberId: {}, 총 시도 횟수: {}", eventId, memberId, tryCount);
                return;
            } catch (OptimisticLockingFailureException e) {
                log.warn("이벤트 참가 재시도 - eventId: {}, memberId: {}, 현재 시도횟수: {}, error: {}",
                         eventId, memberId, tryCount, e.getMessage());
                tryCount++;
                Thread.sleep(RETRY_DELAY);
            }
        }
    }
}
