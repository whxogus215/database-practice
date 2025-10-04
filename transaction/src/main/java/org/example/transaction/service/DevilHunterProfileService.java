package org.example.transaction.service;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transaction.entity.DevilHunterRank;
import org.example.transaction.repository.DevilHunterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DevilHunterProfileService {

    private final DevilHunterRepository devilHunterRepository;
    private final EntityManager em;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<DevilHunterRank> getHunterRankWithReadCommited(final Long id, final CountDownLatch latch) throws InterruptedException {
        List<DevilHunterRank> result = new ArrayList<>();

        final DevilHunterRank firstRank = findDevilHunterRank(id);
        log.info("이 데빌헌터의 등급은 : {} 입니다. ", firstRank);
        result.add(firstRank);

        latch.countDown();
        Thread.sleep(2000);

        em.clear();

        final DevilHunterRank secondRank = findDevilHunterRank(id);
        log.info("이 데빌헌터의 등급은 : {} 입니다. ", secondRank);
        result.add(secondRank);

        return result;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<DevilHunterRank> getHunterRankWithRepeatableRead(final Long id, final CountDownLatch latch) throws InterruptedException {
        List<DevilHunterRank> result = new ArrayList<>();

        final DevilHunterRank firstRank = findDevilHunterRank(id);
        log.info("이 데빌헌터의 등급은 : {} 입니다. ", firstRank);
        result.add(firstRank);

        latch.countDown();
        Thread.sleep(2000);

        em.clear();

        final DevilHunterRank secondRank = findDevilHunterRank(id);
        log.info("이 데빌헌터의 등급은 : {} 입니다. ", secondRank);
        result.add(secondRank);

        return result;
    }

    private DevilHunterRank findDevilHunterRank(final Long id) {
        return devilHunterRepository.findById(id).orElseThrow().getRank();
    }
}
