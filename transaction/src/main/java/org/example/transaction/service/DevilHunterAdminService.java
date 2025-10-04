package org.example.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transaction.entity.DevilHunter;
import org.example.transaction.entity.DevilHunterRank;
import org.example.transaction.repository.DevilHunterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DevilHunterAdminService {

    private final DevilHunterRepository devilHunterRepository;

    @Transactional
    public void updateDevilHunterRank(final Long id, final DevilHunterRank rank) {
        final DevilHunter findDevilHunter = devilHunterRepository.findById(id).orElseThrow();
        log.info("데빌헌터 {}의 등급을 변경합니다.", findDevilHunter.getName());
        findDevilHunter.update(rank);
    }
}
