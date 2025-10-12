package org.example.transaction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transaction.repository.DevilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MissionControlService {

    private final DevilRepository devilRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Integer> findDevilCountFromArea(String area, CountDownLatch latch) throws InterruptedException {
        List<Integer> result = new ArrayList<>();

        int firstCount = devilRepository.findDevilsByArea(area).size();
        result.add(firstCount);
        log.info("[지휘관] {} 구역의 악마는 {}마리", area, firstCount);

        if (latch != null) {
            latch.countDown();
        }

        Thread.sleep(1000);

        int secondCount = devilRepository.findDevilsByArea(area).size();
        result.add(secondCount);
        log.info("[지휘관] {} 구역의 악마는 {}마리", area, secondCount);
        return result;
    }
}
