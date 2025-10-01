package org.example.transaction.aop;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class ConcurrencyTestAspect {

    private static CountDownLatch updateLatch;
    private static CountDownLatch readLatch;

    public static void setUpdateLatchs(CountDownLatch updateLatch, CountDownLatch readLatch) {
        ConcurrencyTestAspect.updateLatch = updateLatch;
        ConcurrencyTestAspect.readLatch = readLatch;
    }

    @Pointcut("execution(* org.example.transaction.repository.DevilFleshRepository.saveAndFlush(..))")
    private void saveAndFlushMethod() {}

    @Around("saveAndFlushMethod()")
    public Object addDelay(ProceedingJoinPoint pjp) throws Throwable {
        log.info("[Aspect] saveAndFlush() 실행 전");
        final Object result = pjp.proceed();
        log.info("[Aspect] DB에 UPDATE 쿼리가 전송된 시점입니다.");

        if (updateLatch != null) {
            log.info("[Aspect] '마키마 스레드'에게 조회 신호를 보냅니다.");
            updateLatch.countDown();
        }

        if (readLatch != null) {
            log.info("[Aspect] '마키마 스레드'가 읽기를 마칠 때까지 기다립니다.");
            readLatch.await(3, TimeUnit.SECONDS);
            log.info("[Aspect] '마키마 스레드'가 읽기를 마쳤습니다.");
        }

        return result;
    }
}
