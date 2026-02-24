package org.example.lock.deadlock.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lock.deadlock.repository.EventWithLockRepository;
import org.example.lock.deadlock.service.EventJoinWithLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NamedLockEventFacade {

    private static final int LOCK_TIMEOUT_SECONDS = 3;
    private static final long RETRY_DELAY = 50;

    private final EventJoinWithLockService eventJoinWithLockService;
    private final EventWithLockRepository eventWithLockRepository;

    @Transactional
    public void participateWithNamedLock(Long eventId, Long memberId) throws InterruptedException {
        int tryCount = 1;
        String lockKey = String.format("event:%d", eventId);

        while (tryCount <= 5) {
            try {
                final Integer lockResult = eventWithLockRepository.getLock(lockKey, LOCK_TIMEOUT_SECONDS);
                if (lockResult != 1) {
                    log.warn("락 획득 실패 - eventId: {}, memberId: {},", eventId, memberId);
                    Thread.sleep(RETRY_DELAY);
                    continue;
                }
                eventJoinWithLockService.joinEventWithNamedLock(eventId, memberId);
                log.info("이벤트 참가 성공 - eventId: {}, memberId: {}, 총 시도 횟수: {}", eventId, memberId, tryCount);
                return ;
            } catch (Exception e) {
                log.warn("이벤트 참가 재시도 - eventId: {}, memberId: {}, 현재 시도횟수: {}, error: {}",
                         eventId, memberId, tryCount, e.getMessage());
                tryCount++;
                Thread.sleep(RETRY_DELAY);
            } finally {
                final Integer releaseResult = eventWithLockRepository.releaseLock(lockKey);
                if (releaseResult != 1) {
                    log.warn("락 해제 실패 - eventId: {}, memberId: {},", eventId, memberId);
                }
            }
        }
    }
}
